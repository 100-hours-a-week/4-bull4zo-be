package com.moa.moa_server.domain.vote.dto.ai_vote;

import io.swagger.v3.oas.annotations.media.Schema;

public record AIVoteCreateResponse(
    @Schema(description = "생성된 투표 ID", example = "123") Long voteId,
    @Schema(description = "생성 성공 여부", example = "true") boolean created) {}
