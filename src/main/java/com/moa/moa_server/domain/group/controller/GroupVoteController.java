package com.moa.moa_server.domain.group.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.group.dto.group_vote.GroupVoteListResponse;
import com.moa.moa_server.domain.group.service.GroupVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "GroupVote", description = "그룹 투표 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupVoteController {

  private final GroupVoteService groupVoteService;

  @Operation(summary = "그룹 투표 목록 조회", description = "그룹에서 생성된 투표 목록을 조회합니다.")
  @GetMapping("{groupId}/votes")
  public ResponseEntity<ApiResponse<GroupVoteListResponse>> getGroupVotes(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long groupId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    GroupVoteListResponse response = groupVoteService.getGroupVotes(userId, groupId, cursor, size);
    return ResponseEntity.status(200).body(new ApiResponse<>("SUCCESS", response));
  }
}
