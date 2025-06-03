package com.moa.moa_server.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드용 URL 요청 DTO")
public record PresignedUrlRequest(
    @Schema(description = "파일명", example = "test.png") String fileName) {}
