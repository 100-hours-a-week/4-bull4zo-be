package com.moa.moa_server.domain.ranking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Top3 투표 조회 응답 DTO")
public record TopVoteResponseV2(
    @Schema(description = "그룹 ID", example = "1") Long groupId,
    @Schema(description = "랭킹 기준 시작 시간", example = "2025-07-21T00:00:00") LocalDateTime rankedFrom,
    @Schema(description = "랭킹 기준 종료 시간", example = "2025-07-21T01:00:00") LocalDateTime rankedTo,
    @Schema(description = "Top3 투표 목록") List<TopVoteItemV2> topVotes) {

  public static TopVoteResponseV2 of(
      Long groupId, LocalDateTime from, LocalDateTime to, List<TopVoteItemV2> votes) {
    return TopVoteResponseV2.builder()
        .groupId(groupId)
        .rankedFrom(from)
        .rankedTo(to)
        .topVotes(votes)
        .build();
  }
}
