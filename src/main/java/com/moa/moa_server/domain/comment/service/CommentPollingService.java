package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 댓글 롱폴링을 위한 진입점 서비스.
 *
 * <p>Spring Web의 {@link DeferredResult}를 사용하여 비동기 응답 처리를 시작하고, 실제 댓글 감지 및 응답 로직은 별도의 비동기 서비스({@link
 * CommentPollingAsyncService})로 위임한다.
 *
 * <p>요청 스레드는 즉시 반환되고, 실제 응답은 댓글이 감지되거나 타임아웃이 발생했을 때 클라이언트로 전송된다.
 *
 * <p>역할: DeferredResult 생성 및 비동기 처리 위임
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentPollingService {

  private final CommentPollingAsyncService asyncService;

  public DeferredResult<CommentListResponse> pollComments(
      Long userId, Long voteId, @Nullable String cursor) {
    // 비동기 응답 컨테이너
    DeferredResult<CommentListResponse> result =
        new DeferredResult<>(CommentPollingConstants.TIMEOUT_MILLIS);

    // 별도 스레드풀에서 롱폴링 작업 진행
    asyncService.pollAsync(userId, voteId, cursor, result);

    // 위에서 비동기 작업 등록 후, DeferredResult를 바로 반환한다. (return result; 실행)
    // - 이 시점에 요청(서블릿) 스레드는 반환되고
    // - 실제 응답은 별도 스레드에서 result.setResult() 호출 시 전송된다.
    return result;
  }
}
