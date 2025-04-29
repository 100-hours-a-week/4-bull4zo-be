package com.moa.moa_server.domain.auth.dto.request;

public record LoginRequestDto(
        String provider,
        String code
) {}