package com.moa.moa_server.domain.vote.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record VoteUpdateRequest(
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "첨부 이미지 URL", example = "https://...s3.amazonaws.com/vote/abc.jpg")
        String imageUrl,
    @Schema(description = "첨부 이미지 이름", example = "이미지.jpeg") String imageName,
    @Schema(description = "투표 종료 일시", example = "2025-06-20T23:59:00") LocalDateTime closedAt) {}
