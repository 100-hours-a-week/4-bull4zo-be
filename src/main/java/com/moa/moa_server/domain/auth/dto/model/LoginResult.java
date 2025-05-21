package com.moa.moa_server.domain.auth.dto.model;

import com.moa.moa_server.domain.auth.dto.response.LoginResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜 로그인 서비스 반환 DTO")
public record LoginResult(
    @Schema(description = "로그인 응답 DTO") LoginResponse loginResponseDto,
    @Schema(description = "리프레시 토큰") String refreshToken) {}
