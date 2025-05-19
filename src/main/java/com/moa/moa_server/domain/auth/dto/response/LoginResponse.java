package com.moa.moa_server.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 응답 DTO")
public record LoginResponse(

        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6..")
        String accessToken,

        @Schema(description = "사용자 ID", example = "123")
        Long userId,

        @Schema(description = "사용자 닉네임", example = "닉네임")
        String nickname
) {}