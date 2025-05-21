package com.moa.moa_server.domain.auth.service;

import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenService {

  @Value("${jwt.secret}")
  private String secretKey;

  private final long accessTokenExpirationMillis = 3600000;
  private SecretKey key;

  @PostConstruct
  protected void init() {
    // 주입받은 secret을 Base64로 디코딩하여 SecretKey 생성
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  // 액세스 토큰 생성
  public String issueAccessToken(Long userId) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + accessTokenExpirationMillis);

    return Jwts.builder()
        .subject(String.valueOf(userId)) // 사용자 정보 저장
        .issuedAt(now) // 발급 시간
        .expiration(expiryDate) // 만료 시간
        .signWith(key) // 서명
        .compact(); // JWS(최종 서명된 JWT) 문자열 생성
  }

  // JWT 토큰 검사 및 userId 추출
  public Long validateAndExtractUserId(String token) {
    try {
      Jws<Claims> claims =
          Jwts.parser().verifyWith(key).build().parseSignedClaims(token); // 유효성 검사 실행

      String subject = claims.getPayload().getSubject();
      return Long.valueOf(subject);
    } catch (ExpiredJwtException e) {
      throw new AuthException(AuthErrorCode.TOKEN_EXPIRED);
    } catch (JwtException e) {
      throw new AuthException(AuthErrorCode.INVALID_TOKEN);
    }
  }

  public int getAccessTokenExpirationSeconds() {
    return (int) (accessTokenExpirationMillis / 1000);
  }
}
