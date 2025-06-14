package com.moa.moa_server.domain.comment.controller;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.service.CommentPollingService;
import com.moa.moa_server.domain.comment.service.handler.CommentAsyncResultHandler;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.security.SecurityContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class CommentPollingController {

  private final CommentPollingService commentPollingService;

  @GetMapping("/{voteId}/comments/poll")
  public DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> pollComments(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestParam(required = false) String cursor,
      HttpServletRequest request,
      HttpServletResponse response) {

    // 비동기 컨트롤러 진입 시 인증 정보를 현재 요청에 수동으로 복원
    SecurityContextUtil.propagateSecurityContextToRequest(request, response);

    // A: 내부 서비스에서 사용하는 DeferredResult
    DeferredResult<CommentListResponse> deferred =
        commentPollingService.pollComments(userId, voteId, cursor);

    // B: 실제 응답에 사용될 DeferredResult
    DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> apiResult =
        new DeferredResult<>(CommentPollingConstants.CONTROLLER_TIMEOUT_MILLIS);

    // DeferredResult 처리
    CommentAsyncResultHandler resultHandler = new CommentAsyncResultHandler(voteId, cursor);
    resultHandler.handle(deferred, apiResult);

    return apiResult;
  }
}
