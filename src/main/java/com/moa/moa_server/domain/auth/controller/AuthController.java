package com.moa.moa_server.domain.auth.controller;

import com.moa.moa_server.domain.auth.dto.model.LoginResult;
import com.moa.moa_server.domain.auth.dto.request.LoginRequestDto;
import com.moa.moa_server.domain.auth.dto.response.LoginResponseDto;
import com.moa.moa_server.domain.auth.service.AuthService;
import com.moa.moa_server.domain.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token-expiration}")
    private long jwtRefreshTokenExpiration;

    @PostMapping("/login/oauth")
    public ResponseEntity<ApiResponse> oAuthLogin(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        try {
            LoginResult dto = authService.login(request.provider(), request.code());
            LoginResponseDto loginResponseDto = dto.loginResponseDto();
            String refreshToken = dto.refreshToken();

            // 리프레시 토큰 쿠키 설정
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMillis(jwtRefreshTokenExpiration))
                    .sameSite("None") // CORS 허용 필요 시
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());

            return ResponseEntity.ok(new ApiResponse("SUCCESS", loginResponseDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse("AUTH_FAILED", null));
        }
    }
}
