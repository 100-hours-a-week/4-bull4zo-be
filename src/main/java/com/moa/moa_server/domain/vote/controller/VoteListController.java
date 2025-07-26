package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.response.active.ActiveVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.mine.MyVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteResponse;
import com.moa.moa_server.domain.vote.service.VoteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "VoteList", description = "투표 목록 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteListController {

  private final VoteListService voteListService;

  @Operation(summary = "진행 중인 투표 목록 조회", description = "사용자가 참여할 수 있는 진행 중인 투표 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<ActiveVoteResponse>> getActiveVotes(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    ActiveVoteResponse response = voteListService.getActiveVotes(userId, groupId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "내가 만든 투표 목록 조회", description = "사용자가 등록한 투표 목록을 조회합니다.")
  @GetMapping("/mine")
  public ResponseEntity<ApiResponse<MyVoteResponse>> getMyVotes(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    MyVoteResponse response = voteListService.getMyVotes(userId, groupId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "내가 참여한 투표 목록 조회", description = "사용자가 참여한 투표 목록을 조회합니다.")
  @GetMapping("/submit")
  public ResponseEntity<ApiResponse<SubmittedVoteResponse>> getSubmittedVotes(
      @AuthenticationPrincipal Long userId,
      @RequestParam(required = false) Long groupId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    SubmittedVoteResponse response =
        voteListService.getSubmittedVotes(userId, groupId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
