package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteResponse;
import com.moa.moa_server.domain.vote.service.v3.VoteServiceV3;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/votes")
@RequiredArgsConstructor
public class VoteControllerV3 {

  private final VoteServiceV3 voteServiceV3;

  @Hidden
  @PostMapping("/{voteId}/submit")
  public ResponseEntity<ApiResponse<Void>> submitVote(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestBody VoteSubmitRequest request) {
    voteServiceV3.submitVote(voteId, userId, request);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
  }

  @Hidden
  @GetMapping("/submit")
  public ResponseEntity<ApiResponse<SubmittedVoteResponse>> getSubmittedVotes(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    SubmittedVoteResponse response = voteServiceV3.getSubmittedVotes(userId, groupId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
