package com.moa.moa_server.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "그룹 생성 요청 응답 DTO")
public record GroupCreateResponse(
    @Schema(description = "생성된 그룹 ID", example = "17") Long groupId,
    @Schema(description = "그룹 이름", example = "춘식이 연구회") String name,
    @Schema(description = "그룹 설명", example = "매일 춘식이 사진 공유") String description,
    @Schema(description = "그룹 이미지 URL", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,
    @Schema(description = "초대 코드", example = "ABC123") String inviteCode,
    @Schema(description = "생성 시각", example = "2025-04-25T14:00:00") LocalDateTime createdAt) {}
