package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.dto.response.CommentItem;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContext;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContextFactory;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.global.exception.BaseException;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 댓글 롱폴링의 핵심 로직을 담당하는 비동기 서비스.
 *
 * <p>지정된 투표와 사용자에 대해 권한 검증을 수행한 뒤, 주기적으로 새로운 댓글을 감지하여 응답한다.
 *
 * <p>{@link org.springframework.scheduling.annotation.Async} 기반으로 별도 스레드풀에서 실행되며, 응답은 {@link
 * DeferredResult#setResult(Object)} 또는 {@link DeferredResult#setErrorResult(Object)}를 통해 비동기 전송된다.
 *
 * <p>역할: 댓글 권한 검증, 롱폴링 루프 수행, 응답 구성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentPollingAsyncService {

  private final CommentRepository commentRepository;
  private final CommentPermissionContextFactory permissionContextFactory;
  private final CommentPollingQueryService pollingQueryService;

  /**
   * 댓글 롱폴링 핵심 비동기 처리 메서드
   *
   * @param userId 요청자 ID
   * @param voteId 대상 투표 ID
   * @param cursor 이전 커서 위치 (nullable)
   * @param result 비동기 결과 객체
   */
  @Async("commentPollingExecutor")
  public void pollAsync(
      Long userId,
      Long voteId,
      @Nullable String cursor,
      DeferredResult<CommentListResponse> result) {

    try {
      // 유저, 투표, 권한 체크 (내부에서 트랜잭션 적용)
      CommentPermissionContext context =
          permissionContextFactory.validateAndGetContext(userId, voteId);
      User user = context.user();
      Vote vote = context.vote();

      // 커서 파싱
      CreatedAtCommentIdCursor parsedCursor =
          cursor != null ? CreatedAtCommentIdCursor.parse(cursor) : null;

      // 롱폴링 루프 수행
      List<Comment> newComments = pollingLoop(vote, user, parsedCursor);

      // 응답 객체 생성 및 반환
      CommentListResponse response = buildResponse(voteId, user, newComments, cursor);
      result.setResult(response);
    } catch (BaseException e) {
      log.error("[CommentPollingService#pollAsync] 롱폴링 중 정의된 예외 발생: {}", e.getCode());
      result.setErrorResult(e);
    } catch (Exception e) {
      log.error("[CommentPollingService#pollAsync] 롱폴링 중 예외 발생: {}", e.getMessage());
      result.setErrorResult(e);
    }
  }

  /**
   * 지정된 투표에 대해 새 댓글을 감시하는 롱폴링 루프. TIMEOUT_MILLIS 까지 INTERVAL_MILLIS 간격으로 새로운 댓글을 조회하며, 새 댓글이 발견되면
   * 즉시 반환한다.
   *
   * @param vote 투표 엔티티
   * @param user 요청자
   * @param parsedCursor 기준 커서 (nullable)
   * @return 새 댓글 목록 (없으면 빈 리스트)
   */
  private List<Comment> pollingLoop(
      Vote vote, User user, @Nullable CreatedAtCommentIdCursor parsedCursor) {

    log.debug("[CommentPollingService#pollingLoop] 롱폴링 시작");

    long startTime = System.currentTimeMillis();

    while (System.currentTimeMillis() - startTime < CommentPollingConstants.TIMEOUT_MILLIS) {

      // 커서 이후에 생성된 댓글 조회 (내부에서 트랜잭션 적용)
      List<Comment> newComments = pollingQueryService.getNewComments(vote, parsedCursor);

      // 새 댓글이 있으면 즉시 반환
      if (!newComments.isEmpty()) {
        log.debug(
            "[CommentPollingService#pollingLoop] 롱폴링 종료 - 새 댓글 감지 count={}", newComments.size());
        return newComments;
      }

      // 새 댓글이 없으면 INTERVAL_MILLIS 만큼 대기 후 재조회
      try {
        Thread.sleep(CommentPollingConstants.INTERVAL_MILLIS);
      } catch (InterruptedException e) {
        // 스레드 인터럽트 발생 시 로그 남기고 즉시 종료
        log.warn(
            "[CommentPollingService#pollingLoop] 롱폴링 스레드 인터럽트 발생: userId={}, voteId={}",
            user.getId(),
            vote.getId(),
            e);
        Thread.currentThread().interrupt();
        break;
      }
    }

    // 타임아웃 도달 또는 인터럽트 발생: 빈 결과 반환
    log.debug("[CommentPollingService#pollingLoop] 롱폴링 종료 - 타임아웃 또는 인터럽트");
    return List.of();
  }

  /**
   * 댓글 목록 응답 객체를 생성.
   *
   * @param voteId 대상 투표 ID
   * @param user 요청자
   * @param comments 새 댓글 목록
   * @param previousCursor 이전 커서 (nullable)
   * @return 응답 DTO
   */
  private CommentListResponse buildResponse(
      Long voteId, User user, List<Comment> comments, @Nullable String previousCursor) {
    int pageSize = CommentPollingConstants.MAX_POLL_SIZE;
    boolean hasNext = comments.size() > pageSize;
    if (hasNext) comments = comments.subList(0, pageSize);

    // 댓글 목록
    List<CommentItem> items =
        comments.stream().map(comment -> CommentItem.of(comment, user)).toList();
    // of 메서드에서 Lazy Loading 발생할 수 있으나, fetch join으로 필요한 연관 엔티티 미리 로딩하여 트랜잭션 외부에서도 안전하게 접근 가능

    // 다음 커서
    String nextCursor =
        comments.isEmpty()
            ? previousCursor
            : new CreatedAtCommentIdCursor(
                    comments.getLast().getCreatedAt(), comments.getLast().getId())
                .encode();

    return new CommentListResponse(voteId, items, nextCursor, false, items.size());
  }
}
