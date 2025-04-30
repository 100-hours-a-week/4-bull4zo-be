package com.moa.moa_server.domain.auth.controller;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.request.LoginRequestDto;
import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;
import com.moa.moa_server.domain.auth.dto.response.TokenRefreshResponseDto;
import com.moa.moa_server.domain.auth.service.AuthService;
import com.moa.moa_server.domain.auth.service.RefreshTokenService;
import com.moa.moa_server.domain.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login/oauth")
    public ResponseEntity<ApiResponse> oAuthLogin(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        try {
            // OAuth 로그인 서비스 로직 수행
            LoginResult dto = authService.login(request.provider(), request.code());
            LoginResponseDto loginResponseDto = dto.loginResponseDto();
            String refreshToken = dto.refreshToken();

            // 리프레시 토큰 쿠키 설정
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(refreshTokenService.getRefreshTokenExpirySeconds())
                    .sameSite("None") // CORS 허용 필요 시
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(new ApiResponse("SUCCESS", loginResponseDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("AUTH_FAILED", null));
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse> refreshAccessToken(HttpServletRequest request) {
        try {
            // 쿠키에서 리프레시 토큰 추출
            String refreshToken = extractRefreshTokenFromCookie(request);

            // 액세스 토큰 재발급 서비스 로직 수행
            TokenRefreshResponseDto tokenRefreshResponseDto = authService.refreshAccessToken(refreshToken);

            return ResponseEntity.ok(new ApiResponse("SUCCESS", tokenRefreshResponseDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("AUTH_FAILED", null));
        }
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies() == null) {
            throw new IllegalArgumentException("No cookies found in request");
        }
        return java.util.Arrays.stream(request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refresh token cookie not found"))
                .getValue();
    }
}
