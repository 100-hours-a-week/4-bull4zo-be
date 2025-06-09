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
      @RequestParam(required = true) String cursor) {
    DeferredResult<CommentListResponse> deferred =
        commentPollingService.pollComments(userId, voteId, cursor);

    DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> apiResult =
        new DeferredResult<>();

    deferred.onCompletion(
        () -> {
          if (deferred.hasResult()) {
            apiResult.setResult(
                ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", (CommentListResponse) deferred.getResult())));
          }
        });
    deferred.onError(deferred::setErrorResult);

    return apiResult;
  }
}
