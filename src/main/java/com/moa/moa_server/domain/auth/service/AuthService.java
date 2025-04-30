package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.response.TokenRefreshResponseDto;
import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.auth.repository.TokenRepository;
import com.moa.moa_server.domain.auth.service.strategy.OAuthLoginStrategy;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.util.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final Map<String, OAuthLoginStrategy> strategies;
    private final TokenRepository tokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

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

        // 토큰 형식이 잘못되었거나, 서명이 유효하지 않거나, 만료된 경우
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new SecurityException("INVALID_TOKEN"); // 401
        }

        // DB에 저장된 리프레시 토큰인지 확인
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SecurityException("FORBIDDEN")); // 403

        // DB에 저장된 토큰의 만료 시간 확인
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("FORBIDDEN"); // 403
        }

        // 사용자 상태 검증
        User user = token.getUser();
        UserValidator.validateActive(user); // 존재하지 않는 유저, 탈퇴한 유저 처리됨

        // 새 액세스 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        int expiresIn = jwtTokenProvider.getAccessTokenExpirationSeconds();

        // 응답 반환
        return new TokenRefreshResponseDto(accessToken, expiresIn);
    }
}
