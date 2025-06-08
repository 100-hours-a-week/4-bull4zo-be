package com.moa.moa_server.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "댓글 작성 요청 DTO")
public record CommentCreateRequest(
    @Schema(description = "댓글 내용", example = "에어컨 추워요") @NotBlank String content,
    @Schema(description = "익명 여부", example = "false") @NotNull Boolean anonymous) {}
