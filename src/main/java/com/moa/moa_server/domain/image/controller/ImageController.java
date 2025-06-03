package com.moa.moa_server.domain.image.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.image.dto.PresignedUrlRequest;
import com.moa.moa_server.domain.image.dto.PresignedUrlResponse;
import com.moa.moa_server.domain.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image", description = "이미지 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/image")
public class ImageController {

  private final ImageService imageService;

  @Operation(
      summary = "Presigend URL 발급",
      description = "이미지 업로드를 위한 presigned url과 file url을 발급합니다.")
  @PostMapping("/presigned-url")
  public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(
      @AuthenticationPrincipal Long userId, @RequestBody PresignedUrlRequest request) {
    PresignedUrlResponse response = imageService.createPresignedUrl(userId, request.fileName());
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
