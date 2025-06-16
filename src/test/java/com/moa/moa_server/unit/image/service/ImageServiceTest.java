package com.moa.moa_server.unit.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.moa.moa_server.domain.image.dto.PresignedUrlResponse;
import com.moa.moa_server.domain.image.handler.ImageErrorCode;
import com.moa.moa_server.domain.image.handler.ImageException;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import java.net.URL;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService")
class ImageServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private S3Presigner s3Presigner;

  @Mock private S3Client s3Client;

  @Mock private User user;

  @InjectMocks private ImageService imageService;

  @Captor private ArgumentCaptor<CopyObjectRequest> copyCaptor;

  @Captor private ArgumentCaptor<DeleteObjectRequest> deleteCaptor;

  private final String bucket = "test-bucket";
  private final String cdnBaseUrl = "https://test-cdn.com";

  @BeforeEach
  void setUp() {
    imageService.setBucket(bucket);
    imageService.setCdnBaseUrl(cdnBaseUrl);
  }

  @Nested
  @DisplayName("이미지 Presigned URL 발급")
  class PresignedUrlTest {

    @BeforeEach
    void setup() {
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    }

    @Test
    void 올바른_파일명으로_presigned_url_발급_성공() throws Exception {
      // given

      // PresignedPutObjectRequest 모킹
      PresignedPutObjectRequest fakeRequest = mock(PresignedPutObjectRequest.class);
      // presign 요청 시 S3 URL을 반환하도록 설정
      when(fakeRequest.url())
          .thenReturn(new URL("https://test-bucket.s3.amazonaws.com/temp/fake.png"));
      // presigner가 어떤 요청이든 fakeRequest를 반환하도록 설정
      when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
          .thenReturn(fakeRequest);

      // when
      PresignedUrlResponse response = imageService.createPresignedUrl(1L, "test.png");

      // then
      assertThat(response.uploadUrl()).contains("https://"); // 업로드용 presigned-url 검증
      assertThat(response.fileUrl()).contains(cdnBaseUrl); // 반환 fileUrl에 버킷명 포함 여부 검증
    }

    @Test
    void 잘못된_확장자_예외발생() {
      // given
      String fileName = "test.txt";

      // when & then
      assertThatThrownBy(() -> imageService.createPresignedUrl(1L, fileName))
          .isInstanceOf(ImageException.class)
          .hasMessageContaining(ImageErrorCode.INVALID_FILE.name());
    }
  }

  @Nested
  @DisplayName("temp → vote/group 이동")
  class MoveImageTest {
    @Test
    void 정상적으로_temp에서_vote로_이미지_이동() {
      // given
      String tempImageUrl = cdnBaseUrl + "/temp/uuid.png";
      String targetDir = "vote";

      // when
      imageService.moveImageFromTempToTarget(tempImageUrl, targetDir);

      // then
      verify(s3Client, times(1)).copyObject(any(Consumer.class));
      verify(s3Client, times(1)).deleteObject(any(Consumer.class));
    }

    @Test
    void S3에_key_없으면_FILE_NOT_FOUND_예외() {
      // given
      String tempImageUrl = cdnBaseUrl + "/temp/uuid.png";
      String targetDir = "vote";
      doThrow(NoSuchKeyException.builder().build()).when(s3Client).copyObject(any(Consumer.class));

      // then
      assertThatThrownBy(() -> imageService.moveImageFromTempToTarget(tempImageUrl, targetDir))
          .isInstanceOf(ImageException.class)
          .hasMessageContaining(ImageErrorCode.FILE_NOT_FOUND.name());
    }

    @Test
    void temp가_아닌_key는_아무것도_안함() {
      // given
      String tempImageUrl = cdnBaseUrl + "/temp/uuid.png";
      // when
      imageService.moveImageFromTempToTarget(tempImageUrl, "vote");

      // then
      verify(s3Client, never()).copyObject(any(CopyObjectRequest.class));
      verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
  }
}
