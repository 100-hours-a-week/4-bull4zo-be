package com.moa.moa_server.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 요청 DTO")
public record LoginRequest(
    @Schema(description = "소셜 로그인 제공자", example = "kakao") String provider,
    @Schema(description = "인가 코드", example = "c12o345d678e9") String code) {}
