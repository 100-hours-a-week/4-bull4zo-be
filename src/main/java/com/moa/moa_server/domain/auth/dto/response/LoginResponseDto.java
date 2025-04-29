package com.moa.moa_server.domain.auth.dto.response;

public record LoginResponseDto(
        String accessToken,
        Long userId,
        String nickname
) {}