package com.moa.moa_server.domain.auth.dto.response;

public record TokenRefreshResponse(
        String accessToken,
        int expiresIn
) {}