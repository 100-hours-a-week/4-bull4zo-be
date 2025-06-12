package com.moa.moa_server.domain.global.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Test", description = "테스트용 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestController {

  // 누구나 호출 가능한 헬스체크용
  @Hidden
  @Operation(summary = " 서버 헬스 체크", description = "누구나 호출 가능한 ping API")
  @GetMapping("/ping")
  public ResponseEntity<String> ping() {
    return ResponseEntity.ok("pong");
  }

  // 토큰이 있어야 호출 가능한 테스트용
  @Hidden
  @Operation(
      summary = "토큰 인증 테스트",
      description = "인증된 사용자만 호출 가능한 테스트용 API",
      security = @SecurityRequirement(name = "bearer-key"))
  @PostMapping("/auth")
  public ResponseEntity<String> authTest(@AuthenticationPrincipal Long userId) {
    return ResponseEntity.ok("Authenticated user ID: " + userId);
  }
}
