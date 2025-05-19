package com.moa.moa_server.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "가입한 그룹 목록 조회 응답 DTO")
public record JoinedGroupResponse(
        @Schema(description = "그룹 목록")
        List<GroupDetail> groups,

        @Schema(description = "현재 페이지의 마지막 항목 기준 커서", example = "카테부_3")
        String nextCursor,

        @Schema(description = "다음 페이지 여부", example = "false")
        boolean hasNext,

        @Schema(description = "현재 받아온 리스트 길이", example = "1")
        int size
) {}