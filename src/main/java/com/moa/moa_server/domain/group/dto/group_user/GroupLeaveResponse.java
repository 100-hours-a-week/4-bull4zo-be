package com.moa.moa_server.domain.group.dto.group_user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 탈퇴 응답 DTO")
public record GroupLeaveResponse(@Schema(description = "탈퇴한 그룹 ID", example = "7") Long groupId) {}
