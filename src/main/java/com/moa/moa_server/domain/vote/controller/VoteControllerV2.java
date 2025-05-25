package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.service.VoteServiceV2;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/votes")
@RequiredArgsConstructor
public class VoteControllerV2 {

  private final VoteServiceV2 voteServiceV2;

  @Hidden
  @PostMapping("/{voteId}/submit")
  public ResponseEntity<ApiResponse<Void>> submitVote(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestBody VoteSubmitRequest request) {
    voteServiceV2.submitVote(voteId, userId, request);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
  }
}
