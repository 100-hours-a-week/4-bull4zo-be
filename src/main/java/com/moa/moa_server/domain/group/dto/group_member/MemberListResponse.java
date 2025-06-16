package com.moa.moa_server.domain.group.dto.group_member;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 멤버 목록 조회 응답 DTO")
public record MemberListResponse(
    @Schema(description = "그룹 ID", example = "31") Long groupId,
    @Schema(description = "멤버 목록") List<MemberItem> members) {}
