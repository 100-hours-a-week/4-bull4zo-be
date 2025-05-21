package com.moa.moa_server.domain.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 생성 요청 DTO")
public record GroupCreateRequest(
    @Schema(description = "그룹 이름", example = "춘식이 연구회") String name,
    @Schema(description = "그룹 설명", example = "매일 춘식이 사진 공유") String description,
    @Schema(description = "그룹 이미지 URL", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl) {}
