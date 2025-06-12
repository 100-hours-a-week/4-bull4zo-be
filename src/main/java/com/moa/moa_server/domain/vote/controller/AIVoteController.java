package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.ai_vote.AIVoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.ai_vote.AIVoteCreateResponse;
import com.moa.moa_server.domain.vote.service.AIVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AIVote", description = "AI 서버가 호출하는 투표 생성 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai/votes")
public class AIVoteController {

  private final AIVoteService aiVoteService;

  @Operation(summary = "AI 생성 투표 등록", description = "AI가 생성한 투표를 등록합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<AIVoteCreateResponse>> createAIVote(
      @RequestBody @Valid AIVoteCreateRequest request) {
    Long voteId = aiVoteService.createVote(request);
    return ResponseEntity.status(201)
        .body(new ApiResponse<>("SUCCESS", new AIVoteCreateResponse(voteId, true)));
  }
}
