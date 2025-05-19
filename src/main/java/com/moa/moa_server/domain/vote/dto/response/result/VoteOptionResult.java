package com.moa.moa_server.domain.vote.dto.response.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "항목별 결과")
public record VoteOptionResult(

        @Schema(description = "항목 번호 (1: Yes, 2: No)", example = "1")
        int optionNumber,

        @Schema(description = "해당 항목에 투표한 수", example = "3")
        int count,

        @Schema(description = "해당 항목 비율 (소수점 포함)", example = "30")
        double ratio
) {}