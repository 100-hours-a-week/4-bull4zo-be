package com.moa.moa_server.domain.auth.service.impl;

import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import com.moa.moa_server.domain.auth.repository.TokenRepository;
import com.moa.moa_server.domain.auth.service.RefreshTokenService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

  private final TokenRepository tokenRepository;
  private static final long refreshTokenExpirySeconds = 7 * 24 * 60 * 60;

  @Override
  @Transactional
  public String issueRefreshToken(User user) {
    // 기존 리프레시 토큰 삭제 (동시 로그인 방지)
    tokenRepository.deleteByUserId(user.getId());

    String refreshToken = UUID.randomUUID().toString();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusSeconds(refreshTokenExpirySeconds);

    Token token =
        Token.builder()
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
    Token token =
        tokenRepository
            .findByRefreshToken(refreshToken)
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_TOKEN));

    // 토큰 만료 여부 확인
    if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
    }

    // 유저 유효성 확인
    AuthUserValidator.validateActive(token.getUser()); // 존재하지 않는 유저, 탈퇴한 유저 처리
    return token;
  }

  @Override
  @Transactional
  public boolean deleteRefreshTokenByUserId(Long userId) {
    int deletedCount = tokenRepository.deleteByUserId(userId);
    return deletedCount > 0;
  }

  @Override
  public long getRefreshTokenExpirySeconds() {
    return refreshTokenExpirySeconds;
  }
}
