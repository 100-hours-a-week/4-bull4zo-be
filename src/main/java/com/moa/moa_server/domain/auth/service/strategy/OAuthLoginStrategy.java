package com.moa.moa_server.domain.auth.service.strategy;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;

public interface OAuthLoginStrategy {
    LoginResult login(String code);
}
