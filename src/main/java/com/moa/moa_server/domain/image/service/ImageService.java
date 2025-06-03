package com.moa.moa_server.domain.image.service;

import com.moa.moa_server.domain.image.dto.PresignedUrlResponse;
import com.moa.moa_server.domain.image.handler.ImageErrorCode;
import com.moa.moa_server.domain.image.handler.ImageException;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class ImageService {

  private final UserRepository userRepository;

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  /** Presigned URL 발급 */
  public PresignedUrlResponse createPresignedUrl(Long userId, String fileName) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    if (!isValidExtension(fileName)) {
      throw new ImageException(ImageErrorCode.INVALID_FILE);
    }

    // S3 key 생성
    String extension = fileName.substring(fileName.lastIndexOf("."));
    String uuid = UUID.randomUUID().toString();
    String key = "temp/" + uuid + extension;

    // S3에 업로드될 객체 정보
    PutObjectRequest objectRequest =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType("image/" + extension.replace(".", ""))
            .build();

    // Presigned URL 요청 생성 (유효기간: 3분)
    PutObjectPresignRequest presignRequest =
        PutObjectPresignRequest.builder()
            .putObjectRequest(objectRequest)
            .signatureDuration(Duration.ofMinutes(3))
            .build();

    // presinged-url 발급
    URL presignedUrl = s3Presigner.presignPutObject(presignRequest).url();

    // fileUrl
    String fileUrl = String.format("https://%s.s3.amazonaws.com/%s", bucket, key);

    return new PresignedUrlResponse(presignedUrl.toString(), fileUrl);
  }

  /** S3의 temp 폴더에서 vote/group 폴더로 이미지를 복사하고 원복 삭제 */
  public void moveImageFromTempToVote(String tempImageUrl, String targetDir) {
    if (tempImageUrl == null || tempImageUrl.isBlank()) return;
    String tempKey = getKeyFromUrl(tempImageUrl); // "temp/uuid.jpg"
    if (!tempKey.startsWith("temp/")) return; // 보안상 체크

    // targetKey: "vote/uuid.jpg" 또는 "group/uuid.jpg"
    String targetKey = tempKey.replaceFirst("temp/", targetDir + "/");

    // S3 복사
    s3Client.copyObject(
        builder ->
            builder
                .copySource(bucket + "/" + tempKey)
                .destinationBucket(bucket)
                .destinationKey(targetKey));
    // S3 원본 삭제
    s3Client.deleteObject(builder -> builder.bucket(bucket).key(tempKey));
  }

  public void validateImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return;
    String expectedPrefix = String.format("https://%s.s3.amazonaws.com", bucket);
    if (!imageUrl.startsWith(expectedPrefix)) {
      throw new ImageException(ImageErrorCode.INVALID_URL);
    }
  }

  /** S3 파일 URL에서 key 값 추출 */
  public static String getKeyFromUrl(String url) {
    int idx = url.indexOf(".amazonaws.com/");
    if (idx == -1) return url;
    return url.substring(idx + ".amazonaws.com/".length());
  }

  private boolean isValidExtension(String fileName) {
    String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png");
  }
}
