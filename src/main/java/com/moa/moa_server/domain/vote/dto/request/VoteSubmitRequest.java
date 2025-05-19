package com.moa.moa_server.domain.vote.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 참여 요청 DTO")
public record VoteSubmitRequest(
        @Schema(description = "사용자가 선택한 항목 번호 (0: 기권, 1: Yes, 2: No)", example = "2")
        int userResponse
) {}
