package com.moa.moa_server.domain.group.dto.group_member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 멤버 정보")
public record MemberItem(
    @Schema(description = "사용자 ID", example = "23") Long userId,
    @Schema(description = "사용자 닉네임", example = "춘식이") String nickname,
    @Schema(description = "그룹 내 역할", example = "OWNER") String role) {}
