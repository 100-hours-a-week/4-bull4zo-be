package com.moa.moa_server.domain.comment.controller;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.handler.CommentAsyncResultHandler;
import com.moa.moa_server.domain.comment.service.CommentPollingService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
import com.moa.moa_server.domain.global.security.SecurityContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Tag(name = "Comment")
@RestController
@RequestMapping("/api/v1/votes")
public class CommentPollingController {

  private final CommentPollingService commentPollingService;
  private final ThreadPoolTaskExecutor executor;

  public CommentPollingController(
      CommentPollingService commentPollingService,
      @Qualifier("commentPollingExecutor") ThreadPoolTaskExecutor executor) {
    this.commentPollingService = commentPollingService;
    this.executor = executor;
  }

  @Operation(summary = "댓글 목록 롱폴링 조회", description = "지정된 시점 이후로 추가된 댓글을 실시간으로 반환합니다.")
  @GetMapping("/{voteId}/comments/poll")
  public DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> pollComments(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestParam(required = false) String cursor,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse) {

    // 비동기 컨트롤러 진입 시 인증 정보를 현재 요청에 수동으로 복원
    SecurityContextUtil.propagateSecurityContextToRequest(httpServletRequest, httpServletResponse);

    // 비동기 응답 객체 생성
    DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> result =
        new DeferredResult<>(CommentPollingConstants.TIMEOUT_MILLIS);

    // 에러/타임아웃 핸들러 등록
    CommentAsyncResultHandler resultHandler = new CommentAsyncResultHandler(voteId, cursor);
    resultHandler.registerHandlers(result);

    // 롱폴링 작업 진행
    executor.execute(
        () -> {
          try {
            CommentListResponse response =
                commentPollingService.pollComments(userId, voteId, cursor);
            result.setResult(ResponseEntity.ok(new ApiResponse<>("SUCCESS", response)));
          } catch (BaseException e) {
            // 정의된 예외 발생 (ex. USER_NOT_FOUND, VOTE_NOT_FOUND, FORBIDDEN, INVALID_CURSOR)
            result.setErrorResult(
                ResponseEntity.status(e.getStatus()).body(new ApiResponse<>(e.getCode(), null)));
          } catch (Exception e) {
            // 위에서 처리되지 않은 예외에 대한 마지막 방어 (ex. 예상치 못한 런타임 예외)
            log.error("[CommentPollingService#pollAsync] 롱폴링 중 예외 발생: {}", e.getMessage());
            result.setErrorResult(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null));
          }
        });

    // 비동기 작업 등록 후, DeferredResult 바로 반환하여 요청 스레드 반환
    return result;
  }
}
