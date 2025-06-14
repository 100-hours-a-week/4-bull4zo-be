package com.moa.moa_server.domain.comment.handler;

import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.GlobalErrorCode;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

/** 댓글 롱폴링에서 DeferredResult 기반 비동기 응답 처리를 담당하는 핸들러 클래스 */
@Slf4j
public class CommentAsyncResultHandler {

  private final Long voteId;
  private final String cursor;

  // DeferredResult가 각 요청마다 고유하게 생성되므로, 이를 처리하는 핸들러도 요청마다 인스턴스가 생겨야 한다.
  public CommentAsyncResultHandler(Long voteId, String cursor) {
    this.voteId = voteId;
    this.cursor = cursor;
  }

  public void registerHandlers(
      DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> deferred) {

    // 예외 처리
    deferred.onError(
        (e) -> {
          log.warn("[CommentPollingController] pollComments 비동기 처리 과정에서 예외 발생: {}", e.getMessage());
          if (e instanceof RejectedExecutionException) {
            // 스레드풀이 꽉 찬 경우 (RejectedExecutionException) 이곳으로 들어옴
            deferred.setErrorResult(
                ResponseEntity.status(503).body(new ApiResponse<>("THREAD_POOL_REJECTED", null)));
          } else {
            deferred.setErrorResult(
                ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
          }
        });

    // 타임아웃 처리
    deferred.onTimeout(
        () -> {
          if (!deferred.hasResult()) {
            log.warn("[CommentPollingController] pollComments 타임아웃 fallback");
            deferred.setResult(
                ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", CommentListResponse.empty(voteId, cursor))));
          }
        });
  }
}
