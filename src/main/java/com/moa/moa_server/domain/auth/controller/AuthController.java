package com.moa.moa_server.domain.auth.controller;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.request.LoginRequest;
import com.moa.moa_server.domain.auth.dto.response.LoginResponse;
import com.moa.moa_server.domain.auth.dto.response.TokenRefreshResponse;
import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import com.moa.moa_server.domain.auth.service.AuthService;
import com.moa.moa_server.domain.auth.service.RefreshTokenService;
import com.moa.moa_server.domain.auth.swagger.LoginResponses;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.dto.ApiResponseVoid;
import com.moa.moa_server.domain.global.swagger.CommonErrorResponses;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final RefreshTokenService refreshTokenService;

  private final UserRepository userRepository;

  @Operation(summary = "소셜 로그인")
  @LoginResponses
  @PostMapping("/login/oauth")
  public ResponseEntity<ApiResponse<LoginResponse>> oAuthLogin(
      @RequestBody LoginRequest request,
      @RequestHeader("X-Redirect-Uri") String redirectUri,
      HttpServletResponse response) {
    // OAuth 로그인 서비스 로직 수행
    LoginResult dto = authService.login(request.provider(), request.code(), redirectUri);
    LoginResponse loginResponseDto = dto.loginResponseDto();
    String refreshToken = dto.refreshToken();

    // 리프레시 토큰 쿠키 설정
    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", refreshToken)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(refreshTokenService.getRefreshTokenExpirySeconds())
            .sameSite("None") // CORS 허용 필요 시
            .build();
    response.addHeader("Set-Cookie", cookie.toString());

    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", loginResponseDto));
  }

  @Operation(summary = "토큰 재발급")
  @PostMapping("/token/refresh")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshAccessToken(
      HttpServletRequest request) {
    // 쿠키에서 리프레시 토큰 추출
    String refreshToken = extractRefreshTokenFromCookie(request);

    // 액세스 토큰 재발급 서비스 로직 수행
    TokenRefreshResponse tokenRefreshResponseDto = authService.refreshAccessToken(refreshToken);

    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", tokenRefreshResponseDto));
  }

  @Operation(
      summary = "로그아웃",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ApiResponseVoid.class)))
      })
  @CommonErrorResponses
  @DeleteMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @AuthenticationPrincipal Long userId,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (userId == null) {
      throw new AuthException(AuthErrorCode.NO_TOKEN);
    }
    // 유저 조회 및 상태 검증
    User user = userRepository.findById(userId).orElse(null);
    AuthUserValidator.validateActive(user);

    // 로그아웃 처리
    boolean logout = authService.logout(userId);

    // refreshToken 쿠키 무효화
    ResponseCookie expiredCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .maxAge(0) // 즉시 만료
            .sameSite("None")
            .build();
    response.addHeader("Set-Cookie", expiredCookie.toString());

    String resultMessage = logout ? "SUCCESS" : "ALREADY_LOGGED_OUT";
    return ResponseEntity.ok(new ApiResponse<>(resultMessage, null));
  }

  private String extractRefreshTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      throw new AuthException(AuthErrorCode.NO_TOKEN);
    }
    return java.util.Arrays.stream(request.getCookies())
        .filter(c -> "refreshToken".equals(c.getName()))
        .findFirst()
        .orElseThrow(() -> new AuthException(AuthErrorCode.NO_TOKEN))
        .getValue();
  }
}
