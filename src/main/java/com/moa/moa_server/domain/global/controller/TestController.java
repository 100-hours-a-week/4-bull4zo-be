package com.moa.moa_server.domain.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    // 누구나 호출 가능한 헬스체크용
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    // 토큰이 있어야 호출 가능한 테스트용
    // SecurityConfig에서 @AuthenticationPrincipal 처리되어 있어야 함
    @PostMapping("/auth")
    public ResponseEntity<String> authTest(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok("Authenticated user ID: " + userId);
    }
}