package com.moa.moa_server.domain.ranking.dto;

import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "Top3 투표 정보")
public record TopVoteItem(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표가 속한 그룹 ID", example = "1") Long groupId,
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "투표 시작 시각", example = "2025-07-20T12:00:00") LocalDateTime createdAt,
    @Schema(description = "투표 종료 시각", example = "2025-07-21T12:00:00") LocalDateTime closedAt,
    @Schema(description = "항목별 결과 리스트") List<VoteOptionResult> results) {
  public static TopVoteItem from(Vote vote, List<VoteOptionResult> results) {
    return TopVoteItem.builder()
        .voteId(vote.getId())
        .groupId(vote.getGroup().getId())
        .content(vote.getContent())
        .createdAt(vote.getOpenAt() != null ? vote.getOpenAt() : vote.getCreatedAt())
        .closedAt(vote.getClosedAt())
        .results(results)
        .build();
  }
}
