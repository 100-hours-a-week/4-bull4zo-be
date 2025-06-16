package com.moa.moa_server.domain.group.dto.group_member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 멤버 내보내기 응답 DTO")
public record MemberDeleteResponse(
    @Schema(description = "추방된 사용자 ID", example = "5") Long userId) {}
