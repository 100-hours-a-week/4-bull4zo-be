package com.moa.moa_server.domain.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드용 URL 응답 DTO")
public record PresignedUrlResponse(
    @Schema(
            description = "S3 Presigned URL (PUT 요청용)",
            example = "https://.../temp/uuid.jpg?X-Amz-...")
        String uploadUrl,
    @Schema(description = "이미지 업로드 후 접근할 수 있는 정적 URL", example = "https://.../temp/uuid.jpg")
        String fileUrl) {}
