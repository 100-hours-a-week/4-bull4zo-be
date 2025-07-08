package com.moa.moa_server.domain.vote.dto.moderation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "검열 결과 콜백 요청 DTO")
public record VoteModerationCallbackRequest(
    @Schema(description = "투표 ID", example = "123") @NotNull Long voteId,
    @Schema(description = "검열 결과", example = "APPROVED") @NotBlank String result,
    @Schema(description = "검열 사유", example = "NONE") @NotBlank String reason,
    @Schema(description = "상세 사유", example = "적절한 표현입니다.") @NotBlank String reasonDetail,
    @Schema(description = "AI 모델 버전 정보", example = "1.0.0") @NotBlank String version) {}
