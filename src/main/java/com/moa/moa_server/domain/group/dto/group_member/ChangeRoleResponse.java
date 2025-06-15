package com.moa.moa_server.domain.group.dto.group_member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 멤버 역할 변경 응답 DTO")
public record ChangeRoleResponse(
    @Schema(description = "역할이 변경된 사용자 ID", example = "7") Long userId,
    @Schema(description = "변경된 역할", example = "MANAGER") String role) {}
