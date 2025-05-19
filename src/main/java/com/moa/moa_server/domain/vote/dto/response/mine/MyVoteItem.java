package com.moa.moa_server.domain.vote.dto.response.mine;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.entity.Vote;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "내가 만든 투표 정보")
public record MyVoteItem(

        @Schema(description = "투표 ID", example = "123")
        Long voteId,

        @Schema(description = "투표가 속한 그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?")
        String content,

        @Schema(description = "투표 진행 상태 ('PENDING': 검토중, 'REJECTED': 등록실패, 'OPEN': 진행중, 'CLOSED': 종료됨)", example = "OPEN")
        String voteStatus,

        @Schema(description = "투표 시작 시각", example = "2025-04-20T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "투표 종료 시각", example = "2025-04-21T12:00:00")
        LocalDateTime closedAt,

        @Schema(description = "항목별 결과 리스트 (voteStatus가 'PENDING', 'REJECTED'인 경우 null)")
        List<VoteOptionResultWithId> results
) {

    public static MyVoteItem from(Vote vote, List<VoteOptionResultWithId> results) {
        String status;
        if (vote.getVoteStatus() == Vote.VoteStatus.REJECTED || vote.getVoteStatus() == Vote.VoteStatus.PENDING) {
            status = vote.getVoteStatus().name();
        } else {
            status = vote.getClosedAt().isAfter(LocalDateTime.now()) ? "OPEN" : "CLOSED";
        }

        return new MyVoteItem(
                vote.getId(),
                vote.getGroup().getId(),
                vote.getContent(),
                status,
                vote.getCreatedAt(),
                vote.getClosedAt(),
                results
        );
    }
}
