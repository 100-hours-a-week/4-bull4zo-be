package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentPollingService {

  private final long TIMEOUT_MILLIS = 10000L; // 최대 대기 시간 (10초)
  private final int INTERVAL_MILLIS = 500; // 폴링 주기 (0.5초)
  private final int MAX_POLL_SIZE = 10; // 한 번에 가져올 댓글 최대 개수

  private final CommentPollingAsyncService asyncService;

  /**
   * 롱폴링 방식으로 새로운 댓글 목록을 반환한다.
   *
   * <p>Spring 비동기 처리(DeferredResult) 기반으로, 새 댓글이 생기면 즉시 응답하고 없으면 최대 TIMEOUT_MILLIS 동안 대기 후 응답한다.
   *
   * @param userId 인증된 유저 ID
   * @param voteId 대상 투표 ID
   * @param cursor (선택) 마지막으로 받은 댓글 커서 (createdAt_commentId)
   * @return DeferredResult<CommentListResponse> - 응답이 준비되면 바로 반환, 없으면 최대 10초간 대기 후 반환
   */
  public DeferredResult<CommentListResponse> pollComments(
      Long userId, Long voteId, @Nullable String cursor) {
    // 비동기 응답 컨테이너
    DeferredResult<CommentListResponse> result = new DeferredResult<>(TIMEOUT_MILLIS);

    // 별도 스레드풀에서 롱폴링 작업 진행
    asyncService.pollAsync(
        userId, voteId, cursor, TIMEOUT_MILLIS, INTERVAL_MILLIS, MAX_POLL_SIZE, result);

    // 위에서 비동기 작업 등록 후, DeferredResult를 바로 반환한다. (return result; 실행)
    // - 이 시점에 요청(서블릿) 스레드는 반환되고
    // - 실제 응답은 별도 스레드에서 result.setResult() 호출 시 전송된다.
    return result;
  }
}
