package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackRequest;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackResponse;
import com.moa.moa_server.domain.vote.service.VoteModerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vote Moderation", description = "AI 서버가 호출하는 투표 검열 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class VoteModerationController {

  private final VoteModerationService voteModerationService;

  @Operation(
      summary = "AI 검열 결과 콜백 처리",
      description = "\"AI 서버가 투표 내용을 검열한 결과를 백엔드 서버에 전달합니다. 해당 API는 AI 서버에서 직접 호출합니다.")
  @PostMapping("/votes/moderation/callback")
  public ResponseEntity<ApiResponse<VoteModerationCallbackResponse>> callback(
      @RequestBody VoteModerationCallbackRequest request) {
    VoteModerationCallbackResponse result = voteModerationService.handleCallback(request);
    return ResponseEntity.status(201).body(new ApiResponse<>("SUCCESS", result));
  }
}
