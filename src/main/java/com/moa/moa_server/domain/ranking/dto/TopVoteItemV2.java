package com.moa.moa_server.domain.ranking.dto;

import com.moa.moa_server.domain.vote.entity.Vote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Top3 투표 정보")
public record TopVoteItemV2(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표가 속한 그룹 ID", example = "1") Long groupId,
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "응답 수", example = "10") int responsesCount,
    @Schema(description = "댓글 수", example = "5") int commentsCount) {
  public static TopVoteItemV2 from(Vote vote, int responsesCount, int commentsCount) {
    return TopVoteItemV2.builder()
        .voteId(vote.getId())
        .groupId(vote.getGroup().getId())
        .content(vote.getContent())
        .responsesCount(responsesCount)
        .commentsCount(commentsCount)
        .build();
  }
}
