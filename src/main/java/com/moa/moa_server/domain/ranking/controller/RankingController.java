package com.moa.moa_server.domain.ranking.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.ranking.dto.TopVoteResponse;
import com.moa.moa_server.domain.ranking.dto.TopVoteResponseV2;
import com.moa.moa_server.domain.ranking.service.RankingService;
import com.moa.moa_server.domain.ranking.service.RankingServiceV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TopVote", description = "랭킹 투표 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class RankingController {

  private final RankingService rankingService;
  private final RankingServiceV2 rankingServiceV2;

  @Operation(summary = "Top3 투표 목록 조회", description = "그룹 내 하루 동안의 Top3 투표 목록을 조회합니다.")
  @GetMapping("/api/v1/votes/top")
  public ResponseEntity<ApiResponse<TopVoteResponse>> getTopVotes(
      @AuthenticationPrincipal Long userId, @RequestParam @Nullable Long groupId) {
    TopVoteResponse response = rankingService.getTopVotes(userId, groupId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(
      summary = "Top3 투표 목록 조회 V2",
      description = "그룹 내 하루 동안의 Top3 투표 목록을 조회합니다. (내용, 응답 수, 댓글 수만 포함)")
  @GetMapping("/api/v2/votes/top")
  public ResponseEntity<ApiResponse<TopVoteResponseV2>> getTopVotesV2(
      @AuthenticationPrincipal Long userId, @RequestParam @Nullable Long groupId) {
    TopVoteResponseV2 response = rankingServiceV2.getTopVotesV2(userId, groupId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
