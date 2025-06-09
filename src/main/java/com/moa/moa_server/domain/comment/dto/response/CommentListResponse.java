package com.moa.moa_server.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "댓글 목록 조회 응답 DTO")
public record CommentListResponse(
    @Schema(description = "투표 ID") Long voteId,
    @Schema(description = "댓글 목록") List<CommentItem> comments,
    @Schema(description = "현재 페이지의 마지막 항목 기준 커서", example = "2025-04-21T12:00:00_123")
        String nextCursor,
    @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext,
    @Schema(description = "현재 받아온 리스트 길이", example = "10") int size) {}
