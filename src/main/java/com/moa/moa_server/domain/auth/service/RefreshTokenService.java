package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.entity.Token;
import com.moa.moa_server.domain.user.entity.User;

public interface RefreshTokenService {
    String issueRefreshToken(User user); // 생성 + 저장
    Token getValidRefreshToken(String refreshToken); // 존재 + 만료 여부 확인 및 반환
    void deleteRefreshToken(String refreshToken); // 로그아웃
    long getRefreshTokenExpirySeconds();
}
