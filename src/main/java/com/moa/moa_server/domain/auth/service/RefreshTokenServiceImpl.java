package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.auth.repository.TokenRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.util.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final TokenRepository tokenRepository;
    private static final long refreshTokenExpirySeconds = 7 * 24 * 60 * 60;

    @Override
    public String issueRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(refreshTokenExpirySeconds);

        Token token = Token.builder()
                .refreshToken(refreshToken)
                .user(user)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .build();

        tokenRepository.save(token);
        return refreshToken;
    }

    @Override
    public Token getValidRefreshToken(String refreshToken) {
        // 토큰 DB 존재 여부 확인
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new SecurityException("FORBIDDEN"));

        // 토큰 만료 여부 확인
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new SecurityException("FORBIDDEN");
        }

        // 유저 유효성 확인
        UserValidator.validateActive(token.getUser()); // 존재하지 않는 유저, 탈퇴한 유저 처리
        return token;
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        tokenRepository.deleteByRefreshToken(refreshToken);
    }

    @Override
    public long getRefreshTokenExpirySeconds() {
        return refreshTokenExpirySeconds;
    }
}
