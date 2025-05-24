package com.moa.moa_server.domain.test.controller;

import com.moa.moa_server.domain.auth.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-login")
@Profile("local")
@RequiredArgsConstructor
public class TestLoginController {

  private final JwtTokenService jwtTokenService;

  @PostMapping
  public ResponseEntity<String> login(@RequestParam Long userId) {
    String token = jwtTokenService.issueAccessToken(userId);
    return ResponseEntity.ok(token);
  }
}
