package com.moa.moa_server.domain.group.dto.group_vote;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 투표 목록 조회 응답 DTO")
public record GroupVoteListResponse(
    @Schema(description = "그룹 ID", example = "3") Long groupId,
    @Schema(description = "그룹 이름", example = "KTB") String groupName,
    @Schema(description = "투표 목록") List<GroupVoteItem> votes,
    @Schema(
            description = "현재 페이지의 마지막 항목 기준 커서(createdAt_voteId)",
            example = "2025-04-21T12:00:00_123")
        String nextCursor,
    @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext,
    @Schema(description = "현재 받아온 리스트 길이", example = "1") int size) {}
