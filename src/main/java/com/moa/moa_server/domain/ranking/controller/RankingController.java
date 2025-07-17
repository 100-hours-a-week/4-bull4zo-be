package com.moa.moa_server.domain.ranking.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.ranking.dto.TopVoteResponse;
import com.moa.moa_server.domain.ranking.service.RankingService;
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
@RequestMapping("/api/v1/votes/top")
public class RankingController {

  private final RankingService rankingService;

  @Operation(summary = "Top3 투표 목록 조회", description = "그룹 내 하루 동안의 Top3 투표 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<TopVoteResponse>> getTopVotes(
      @AuthenticationPrincipal Long userId, @RequestParam @Nullable Long groupId) {
    TopVoteResponse response = rankingService.getTopVotes(userId, groupId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
