package com.moa.moa_server.domain.ai.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    // AI 검열 결과 수신용
    // `/api/v1/ai/votes/moderation/callback`
//    @PostMapping("/votes/moderation/callback")
//    public ResponseEntity<String> aiVotesTest() {
//        return ResponseEntity.ok("AI vote received (mock)");
//    }
}
