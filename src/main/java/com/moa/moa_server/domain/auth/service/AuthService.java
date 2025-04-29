package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;
import com.moa.moa_server.domain.auth.service.strategy.OAuthLoginStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Map<String, OAuthLoginStrategy> strategies;

    public LoginResponseDto login(String provider, String code) {
        OAuthLoginStrategy strategy = strategies.get(provider.toLowerCase());
        if (strategy == null) throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다: " + provider);
        return strategy.login(code);
    }
}
