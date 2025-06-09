package com.moa.moa_server.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "댓글 정보")
public record CommentItem(
    @Schema(description = "댓글 ID", example = "42") Long commentId,
    @Schema(description = "댓글 내용", example = "저도 그렇게 생각해요.") String content,
    @Schema(description = "댓글 작성자 닉네임", example = "익명1") String authorNickname,
    @Schema(description = "댓글 작성 시각", example = "2025-04-22T14:30:00") LocalDateTime createdAt,
    @Schema(description = "사용자가 댓글 작성자인지 여부", example = "true") boolean isMine,
    @Schema(description = "숨겨진 댓글 여부", example = "false") boolean hidden,
    @Schema(description = "사용자가 댓글을 신고했는지 여부", example = "false") boolean reportByUser) {}
