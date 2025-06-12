package com.moa.moa_server.domain.vote.dto.ai_vote;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record AIVoteCreateRequest(
    @Schema(description = "투표 내용", example = "당신의 점심은 어떤가요? 왼쪽: 비빔밥, 오른쪽: 제육덮밥") @NotBlank
        String content,
    @Schema(description = "이미지 URL") String imageUrl,
    @Schema(description = "이미지 이름") String imageName,
    @Schema(description = "투표 시작 일시", example = "2025-06-20T10:00:00") @NotBlank
        LocalDateTime openAt,
    @Schema(description = "투표 종료 일시", example = "2025-06-27T10:00:00") @NotBlank
        LocalDateTime closedAt) {}
