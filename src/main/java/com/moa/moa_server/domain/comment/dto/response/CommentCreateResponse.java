package com.moa.moa_server.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "댓글 작성 응답 DTO")
public record CommentCreateResponse(
    @Schema(description = "생성된 댓글 ID", example = "42") Long commentId,
    @Schema(description = "댓글 내용", example = "저도 그렇게 생각해요!") String content,
    @Schema(description = "댓글 작성자 닉네임", example = "익명1") String authorNickname,
    @Schema(description = "댓글 작성 시각", example = "2025-04-25T12:00:00") LocalDateTime createdAt) {}
