package com.moa.moa_server.domain.comment.controller;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.service.CommentPollingService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
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

    // ===== polling 처리 (A) =====

    // 1. 서비스 polling 완료 시 결과 래핑 후 클라이언트 응답
    deferred.setResultHandler(
        (rawValue) -> {
          // setResult, setErrorResult 으로 전달된 결과 처리
          log.info("[CommentPollingController] pollComments 완료");
          if (rawValue instanceof CommentListResponse data) {
            // 롱폴링 로직이 성공적으로 처리됨
            apiResult.setResult(ResponseEntity.ok(new ApiResponse<>("SUCCESS", data)));
          } else if (rawValue instanceof BaseException e) {
            // 정의된 예외 발생 (ex. USER_NOT_FOUND, VOTE_NOT_FOUND, FORBIDDEN)
            apiResult.setErrorResult(
                ResponseEntity.status(e.getStatus()).body(new ApiResponse<>(e.getCode(), null)));
          } else {
            // 그 이외 예외 발생
            apiResult.setErrorResult(
                ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
          }
        });

    // 2. 비동기 처리 과정에서 예외 발생 시 에러 응답 반환
    deferred.onError(
        (e) -> {
          log.warn("[CommentPollingController] pollComments 비동기 처리 과정에서 에러 발생: {}", e.getMessage());
          apiResult.setErrorResult(
              ResponseEntity.internalServerError()
                  .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
        });

    // 3. 서비스 polling 타임아웃 발생
    deferred.onTimeout(
        () -> {
          log.warn("[CommentPollingController] pollComments 타임아웃 fallback");
          if (!deferred.hasResult()) {
            apiResult.setErrorResult(
                ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
          }
        });

    // ===== 클라이언트 응답 완료 후 로그 등 사후처리 (B) =====
    apiResult.onCompletion(() -> log.info("[CommentPollingController] API 응답 완료"));
    apiResult.onError((e) -> log.error("[CommentPollingController] API 응답 처리 중 에러 발생", e));

    return apiResult;
  }
}
