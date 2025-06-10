package com.moa.moa_server.domain.comment.controller;

import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.service.CommentPollingService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class CommentPollingController {

  private final CommentPollingService commentPollingService;

  @GetMapping("/{voteId}/comments/poll")
  public DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> pollComments(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestParam(required = false) String cursor) {
    // 서비스에 polling 요청 (비동기)
    DeferredResult<CommentListResponse> deferred =
        commentPollingService.pollComments(userId, voteId, cursor);

    // API 응답 포맷으로 wrapping
    DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> apiResult =
        new DeferredResult<>();

    // polling 결과가 오면 바로 클라이언트에 응답
    deferred.onCompletion(
        () -> new ApiResponse<>("SUCCESS", (CommentListResponse) deferred.getResult()));
    deferred.onError(deferred::setErrorResult);

    return apiResult;
  }
}
