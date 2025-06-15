package com.moa.moa_server.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "댓글 삭제 응답 DTO")
public record CommentDeleteResponse(
    @Schema(description = "삭제된 댓글 ID", example = "42") Long commentId) {}
