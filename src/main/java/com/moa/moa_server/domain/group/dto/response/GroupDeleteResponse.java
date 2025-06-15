package com.moa.moa_server.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 삭제 응답 DTO")
public record GroupDeleteResponse(
    @Schema(description = "삭제된 그룹 ID", example = "17") Long groupId) {}
