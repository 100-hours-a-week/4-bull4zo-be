package com.moa.moa_server.domain.comment.service.handler;

import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.exception.BaseException;
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

  public void handle(
      DeferredResult<CommentListResponse> deferred,
      DeferredResult<ResponseEntity<ApiResponse<CommentListResponse>>> apiResult) {

    // 결과 처리
    deferred.setResultHandler(
        rawValue -> {
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
            // 위에서 처리되지 않은 예외에 대한 마지막 방어 (ex. 예상치 못한 런타임 예외)
            apiResult.setErrorResult(
                ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
          }
        });

    // 예외 처리
    deferred.onError(
        (e) -> {
          log.warn("[CommentPollingController] pollComments 비동기 처리 과정에서 예외 발생: {}", e.getMessage());
          if (e instanceof RejectedExecutionException) {
            // 스레드풀이 꽉 찬 경우 (RejectedExecutionException) 이곳으로 들어옴
            apiResult.setErrorResult(
                ResponseEntity.status(503).body(new ApiResponse<>("THREAD_POOL_REJECTED", null)));
          } else {
            apiResult.setErrorResult(
                ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(GlobalErrorCode.UNEXPECTED_ERROR.name(), null)));
          }
        });

    // 타임아웃 처리
    deferred.onTimeout(
        () -> {
          if (!deferred.hasResult()) {
            log.warn("[CommentPollingController] pollComments 타임아웃 fallback");
            apiResult.setResult(
                ResponseEntity.ok(
                    new ApiResponse<>("SUCCESS", CommentListResponse.empty(voteId, cursor))));
          }
        });

    // 클라이언트 응답 완료 후 로그 등 사후처리
    apiResult.onCompletion(() -> log.info("[CommentPollingController] API 응답 완료"));
    apiResult.onError((e) -> log.error("[CommentPollingController] API 응답 처리 중 에러 발생", e));
  }
}
