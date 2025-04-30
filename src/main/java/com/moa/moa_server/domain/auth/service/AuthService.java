package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.response.TokenRefreshResponseDto;
import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.auth.service.strategy.OAuthLoginStrategy;
import com.moa.moa_server.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Map<String, OAuthLoginStrategy> strategies;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public LoginResult login(String provider, String code) {
        OAuthLoginStrategy strategy = strategies.get(provider.toLowerCase());
        if (strategy == null) throw new IllegalArgumentException("지원하지 않는 로그인 제공자입니다: " + provider);
        return strategy.login(code);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponseDto refreshAccessToken(String refreshToken) {
        // 토큰이 존재하지 않는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("NO_TOKEN"); // 401
        }

        // 토큰 검증
        Token token = refreshTokenService.getValidRefreshToken(refreshToken);
        User user = token.getUser();

        // 새 액세스 토큰 발급
        String accessToken = jwtTokenService.createAccessToken(user.getId());
        int expiresIn = jwtTokenService.getAccessTokenExpirationSeconds();

        // 응답 반환
        return new TokenRefreshResponseDto(accessToken, expiresIn);
    }
}
