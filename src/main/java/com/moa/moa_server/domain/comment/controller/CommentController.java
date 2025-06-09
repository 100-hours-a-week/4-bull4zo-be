package com.moa.moa_server.domain.comment.controller;

import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.dto.response.CommentCreateResponse;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.service.CommentService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CommentController {

  private final CommentService commentService;

  @Operation(summary = "댓글 작성", description = "한 투표에 대해 댓글을 작성합니다.")
  @PostMapping("/votes/{voteId}/comments")
  public ResponseEntity<ApiResponse<CommentCreateResponse>> createComment(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestBody @Valid CommentCreateRequest request) {
    CommentCreateResponse response = commentService.createComment(userId, voteId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "댓글 목록 조회", description = "투표에 대한 댓글 목록을 조회합니다.")
  @GetMapping("/votes/{voteId}/comments")
  public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Integer size) {
    CommentListResponse response = commentService.getComments(userId, voteId, cursor, size);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
