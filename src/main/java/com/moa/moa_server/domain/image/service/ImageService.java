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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

  private final UserRepository userRepository;

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Setter
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cdn.image-base-url}")
  private String cdnBaseUrl;

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

    try {
      // S3 key 생성
      String extension = fileName.substring(fileName.lastIndexOf("."));
      String contentType = getContentType(extension);
      String uuid = UUID.randomUUID().toString();
      String key = "temp/" + uuid;

      // S3에 업로드될 객체 정보
      PutObjectRequest objectRequest =
          PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build();

      // Presigned URL 요청 생성 (유효기간: 3분)
      PutObjectPresignRequest presignRequest =
          PutObjectPresignRequest.builder()
              .putObjectRequest(objectRequest)
              .signatureDuration(Duration.ofMinutes(3))
              .build();

      // presinged-url 발급
      URL presignedUrl = s3Presigner.presignPutObject(presignRequest).url();

      // fileUrl
      String fileUrl = String.format("%s/%s", cdnBaseUrl, key);

      return new PresignedUrlResponse(presignedUrl.toString(), fileUrl);
    } catch (S3Exception e) {
      throw new ImageException(ImageErrorCode.AWS_S3_ERROR);
    }
  }

  /** S3의 temp 폴더에서 vote/group 폴더로 이미지를 복사하고 원복 삭제 */
  public void moveImageFromTempToTarget(String tempImageUrl, String targetDir) {
    if (tempImageUrl == null || tempImageUrl.isBlank()) return;
    String tempKey = getKeyFromUrl(tempImageUrl); // "temp/uuid"
    if (!tempKey.startsWith("temp/")) return; // 보안상 체크

    // targetKey: "vote/uuid" 또는 "group/uuid"
    String targetKey = tempKey.replaceFirst("temp/", targetDir + "/");

    log.info(
        "[ImageService#moveImageFromTempToTarget] 파일 이동 시도: tempKey {} to targetKey {}",
        tempKey,
        targetKey);

    try {
      // S3 복사
      s3Client.copyObject(
          builder ->
              builder
                  .copySource(bucket + "/" + tempKey)
                  .destinationBucket(bucket)
                  .destinationKey(targetKey));
      // S3 원본 삭제
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(tempKey));

      log.info("[ImageService#moveImageFromTempToTarget] 파일 이동 성공");
    } catch (NoSuchKeyException e) {
      throw new ImageException(ImageErrorCode.FILE_NOT_FOUND);
    } catch (S3Exception e) {
      throw new ImageException(ImageErrorCode.AWS_S3_ERROR);
    }
  }

  public void deleteImage(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return;
    String key = getKeyFromUrl(imageUrl);
    try {
      s3Client.deleteObject(builder -> builder.bucket(bucket).key(key));
    } catch (S3Exception e) {
      throw new ImageException(ImageErrorCode.AWS_S3_ERROR);
    }
  }

  public void validateImageUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) return;
    if (!imageUrl.startsWith(cdnBaseUrl)) {
      throw new ImageException(ImageErrorCode.INVALID_URL);
    }
  }

  /**
   * 투표 이미지 수정 시 S3 이미지 이동/삭제/검증 로직을 처리한다.
   *
   * <p>1. 새 이미지가 없으면(oldImageUrl만 존재) 기존 이미지를 S3에서 삭제<br>
   * 2. 새 이미지가 기존과 동일한 vote/ 경로면(이미 등록된 이미지) 그대로 유지<br>
   * 3. 새 이미지가 temp/ 경로면 S3에서 vote/ 경로로 이동, 기존 이미지는 삭제<br>
   * 4. 그 외 경로는 INVALID_URL 예외 발생
   *
   * @param oldImageUrl 기존 이미지 URL (null/빈값 허용)
   * @param newImageUrl 새 이미지 URL (null/빈값: 이미지 삭제)
   * @return S3에 저장된(이동된) 최종 이미지 URL, 이미지가 없으면 빈 문자열 반환
   * @throws ImageException 잘못된 이미지 경로이거나 S3 작업 실패 등 예외
   */
  public String processImageOnVoteUpdate(String oldImageUrl, String newImageUrl) {
    // 1. 새 이미지가 없으면 기존 이미지 삭제 후 빈 문자열 반환
    if (newImageUrl == null || newImageUrl.isBlank()) {
      deleteImage(oldImageUrl);
      return "";
    }

    // 이미지 URL 형식/버킷 검증
    validateImageUrl(newImageUrl);
    String key = getKeyFromUrl(newImageUrl);

    // 2. 기존 이미지 유지
    if (key.startsWith("vote/")) {
      // old/new가 완전히 같으면 OK (이미 등록된 이미지)
      if (newImageUrl.equals(oldImageUrl)) {
        return oldImageUrl;
      }
      // vote/ 경로지만 값이 다르면, presigned-url 발급/업로드 규칙 위반
      throw new ImageException(ImageErrorCode.INVALID_IMAGE_REUSE);
    }
    // 3. 새로운 이미지 업로드
    else if (key.startsWith("temp/")) {
      moveImageFromTempToVote(newImageUrl, "vote");
      deleteImage(oldImageUrl);
      return newImageUrl.replace("/temp/", "/vote/");
    } else {
      throw new ImageException(ImageErrorCode.INVALID_URL);
    }
  }

  /** S3 파일 URL에서 key 값 추출 */
  public String getKeyFromUrl(String url) {
    int idx = url.indexOf(cdnBaseUrl + "/");
    if (idx == -1) throw new ImageException(ImageErrorCode.INVALID_URL);
    String key = url.substring(idx + (cdnBaseUrl + "/").length());
    if (key.isBlank()) throw new ImageException(ImageErrorCode.INVALID_URL);
    return key;
  }

  private static String getContentType(String extension) {
    extension = extension.toLowerCase();
    return switch (extension) {
      case ".jpg", ".jpeg" -> "image/jpeg";
      case ".png" -> "image/png";
      default -> throw new ImageException(ImageErrorCode.INVALID_FILE);
    };
  }

  private boolean isValidExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) return false;
    String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
    return ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png");
  }
}
