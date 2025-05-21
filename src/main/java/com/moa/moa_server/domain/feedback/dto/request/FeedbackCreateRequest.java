package com.moa.moa_server.domain.feedback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 피드백 등록 요청 DTO")
public record FeedbackCreateRequest(
    @Schema(description = "피드백 내용", example = "이러이러한 기능이 있으면 좋겠어요.") String content) {}
