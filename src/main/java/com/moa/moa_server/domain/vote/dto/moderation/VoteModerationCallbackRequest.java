package com.moa.moa_server.domain.vote.dto.moderation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검열 결과 콜백 요청 DTO")
public record VoteModerationCallbackRequest(

        @Schema(description = "투표 ID", example = "123")
        Long voteId,

        @Schema(description = "검열 결과", example = "APPROVED")
        String result,

        @Schema(description = "검열 사유", example = "NONE")
        String reason,

        @Schema(description = "상세 사유", example = "적절한 표현입니다.")
        String reasonDetail,

        @Schema(description = "AI 모델 버전 정보", example = "1.0.0")
        String version
) {}
