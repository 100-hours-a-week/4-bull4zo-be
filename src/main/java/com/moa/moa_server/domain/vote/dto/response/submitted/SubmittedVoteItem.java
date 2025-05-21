package com.moa.moa_server.domain.vote.dto.response.submitted;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.entity.Vote;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내가 참여한 투표 정보")
public record SubmittedVoteItem(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표가 속한 그룹 ID", example = "1") Long groupId,
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "투표 시작 시각", example = "2025-04-20T12:00:00") LocalDateTime createdAt,
    @Schema(description = "투표 종료 시각", example = "2025-04-21T12:00:00") LocalDateTime closedAt,
    @Schema(description = "항목별 결과 리스트 (voteStatus가 'PENDING', 'REJECTED'인 경우 null)")
        List<VoteOptionResultWithId> results) {
  public static SubmittedVoteItem from(Vote vote, List<VoteOptionResultWithId> results) {
    return new SubmittedVoteItem(
        vote.getId(),
        vote.getGroup().getId(),
        vote.getContent(),
        vote.getCreatedAt(),
        vote.getClosedAt(),
        results);
  }
}
