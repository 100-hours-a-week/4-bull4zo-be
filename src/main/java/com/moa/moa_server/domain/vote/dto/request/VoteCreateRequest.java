package com.moa.moa_server.domain.vote.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "투표 등록 요청 DTO")
public record VoteCreateRequest(
        @Schema(description = "투표가 속할 그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?")
        String content,

        @Schema(description = "첨부 이미지 URL", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,

        @Schema(description = "투표 종료 일시", example = "2025-04-21T12:00:00")
        LocalDateTime closedAt,

        @Schema(description = "투표 등록 익명 여부", example = "false")
        Boolean anonymous
) {}
