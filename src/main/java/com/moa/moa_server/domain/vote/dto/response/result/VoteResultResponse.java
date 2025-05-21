package com.moa.moa_server.domain.vote.dto.response.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "투표 결과 조회 응답 DTO")
public record VoteResultResponse(
    @Schema(description = "조회한 투표 ID", example = "123") Long voteId,
    @Schema(description = "사용자가 선택한 항목 번호 (0: 기권, 1: Yes, 2: No, null: 미참여)", example = "2")
        Integer userResponse,
    @Schema(description = "전체 투표 수", example = "10") int totalCount,
    @Schema(description = "항목별 결과 리스트") List<VoteOptionResult> results) {}
