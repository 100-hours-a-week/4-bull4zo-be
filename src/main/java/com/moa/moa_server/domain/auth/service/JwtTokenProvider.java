package com.moa.moa_server.domain.auth.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMillis;

    private SecretKey key;

    @PostConstruct
    protected void init() {
        // 주입받은 secret을 Base64로 디코딩하여 SecretKey 생성
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 액세스 토큰 생성
    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId)) // 사용자 정보 저장
                .issuedAt(now)                  // 발급 시간
                .expiration(expiryDate)         // 만료 시간
                .signWith(key)                  // 서명
                .compact();                     // JWS(최종 서명된 JWT) 문자열 생성
    }

    // JWT 토큰 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token); // 유효성 검사 실행: 서명 위조 여부, 만료 여부, 형식 오류
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public int getAccessTokenExpirationSeconds() {
        return (int) (accessTokenExpirationMillis / 1000);
    }
}
