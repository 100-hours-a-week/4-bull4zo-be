package com.moa.moa_server.domain.auth.dto.model;

import com.moa.moa_server.domain.auth.dto.response.LoginResponse;

public record LoginResult(
        LoginResponse loginResponseDto,
        String refreshToken
) {}
