package com.moa.moa_server.domain.auth.dto.model;

import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;

public record LoginResult(
        LoginResponseDto loginResponseDto,
        String refreshToken
) {}
