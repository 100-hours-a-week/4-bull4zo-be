package com.moa.moa_server.domain.auth.dto.request;

public record LoginRequest(
        String provider,
        String code
) {}