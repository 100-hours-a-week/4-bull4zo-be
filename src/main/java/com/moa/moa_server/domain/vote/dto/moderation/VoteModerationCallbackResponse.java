package com.moa.moa_server.domain.vote.dto.moderation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 내용 검열 요청 DTO")
public record VoteModerationCallbackResponse(
        @Schema(description = "투표 ID", example = "123")
        Long voteId,

        @Schema(description = "결과가 정상 저장되었는지 여부", example = "true")
        boolean stored
) {}
