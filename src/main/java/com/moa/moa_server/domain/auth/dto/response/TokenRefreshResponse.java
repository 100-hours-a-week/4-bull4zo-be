package com.moa.moa_server.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 재발급 응답 DTO")
public record TokenRefreshResponse(

        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6..")
        String accessToken,

        @Schema(description = "만료 시간", example = "3600")
        int expiresIn
) {}