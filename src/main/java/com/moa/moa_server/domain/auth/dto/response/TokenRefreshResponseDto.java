package com.moa.moa_server.domain.auth.dto.response;

public record TokenRefreshResponseDto(
        String accessToken,
        int expiresIn
) {}