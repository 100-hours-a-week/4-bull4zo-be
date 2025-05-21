package com.moa.moa_server.domain.vote.dto.moderation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 내용 검열 요청 DTO")
public record VoteModerationRequest(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표 내용", example = "에어컨 추우신 분?") String content) {}
