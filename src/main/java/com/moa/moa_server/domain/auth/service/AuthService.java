package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.response.TokenRefreshResponse;
import com.moa.moa_server.domain.auth.entity.OAuth;
import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
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
        if (!OAuth.ProviderCode.isSupported(provider)) {
            throw new AuthException(AuthErrorCode.INVALID_PROVIDER);
        }

        OAuthLoginStrategy strategy = strategies.get(provider.toLowerCase());
        return strategy.login(code);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        // 토큰이 존재하지 않는 경우
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.NO_TOKEN);
        }

        // 토큰 검증
        Token token = refreshTokenService.getValidRefreshToken(refreshToken);
        User user = token.getUser();

        // 새 액세스 토큰 발급
        String accessToken = jwtTokenService.issueAccessToken(user.getId());
        int expiresIn = jwtTokenService.getAccessTokenExpirationSeconds();

        // 응답 반환
        return new TokenRefreshResponse(accessToken, expiresIn);
    }

    public boolean logout(Long userId) {
        return refreshTokenService.deleteRefreshTokenByUserId(userId); // true면 SUCCESS, false면 ALREADY_LOGGED_OUT
    }

    public void unlinkKakaoAccount(Long kakaoUserId) {
        OAuthLoginStrategy strategy = strategies.get("kakao");
        if (strategy != null) {
            strategy.unlink(kakaoUserId);
        }
    }
}
