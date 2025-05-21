package com.moa.moa_server.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 가입 응답 DTO")
public record GroupJoinResponse(
    @Schema(description = "가입된 그룹 ID", example = "7") Long groupId,
    @Schema(description = "그룹 이름", example = "카테부") String groupName,
    @Schema(description = "가입된 역할", example = "MEMBER") String role) {}
