package com.moa.moa_server.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        Long userId,
        String nickname
) {}