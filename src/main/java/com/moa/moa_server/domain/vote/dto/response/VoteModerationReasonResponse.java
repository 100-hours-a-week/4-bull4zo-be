package com.moa.moa_server.domain.vote.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record VoteModerationReasonResponse(
    @Schema(description = "조회한 투표 ID", example = "123") Long voteId,
    @Schema(description = "검열 사유", example = "OFFENSIVE_LANGUAGE") String reviewReason) {}
