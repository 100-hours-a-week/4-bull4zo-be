package com.moa.moa_server.domain.group.dto.group_vote;

import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "투표 목록")
public record GroupVoteItem(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표 본문 내용", example = "야식 메뉴 추천해주세요!") String content,
    @Schema(description = "투표 시작 시간", example = "2025-04-22T20:00:00") LocalDateTime createdAt,
    @Schema(description = "투표 종료 시간", example = "2025-04-23T20:00:00") LocalDateTime closedAt,
    @Schema(description = "항목별 결과 리스트") List<VoteOptionResult> results) {}
