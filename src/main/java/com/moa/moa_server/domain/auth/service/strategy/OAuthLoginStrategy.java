package com.moa.moa_server.domain.auth.service.strategy;

import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;

public interface OAuthLoginStrategy {
    LoginResponseDto login(String code);
}
