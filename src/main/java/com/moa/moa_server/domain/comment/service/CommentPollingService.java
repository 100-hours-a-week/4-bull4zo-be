package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.dto.response.CommentItem;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContext;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContextFactory;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentPollingService {

  private final long TIMEOUT_MILLIS = 10000L; // 최대 대기 시간 (10초)
  private final int INTERVAL_MILLIS = 500; // 폴링 주기 (0.5초)
  private final int MAX_POLL_SIZE = 10; // 한 번에 가져올 댓글 최대 개수

  private final CommentRepository commentRepository;
  private final CommentPermissionContextFactory permissionContextFactory;

  /**
   * 롱폴링 방식으로 새로운 댓글 목록을 반환한다.
   *
   * @param userId 인증된 유저 ID
   * @param voteId 대상 투표 ID
   * @param cursor (선택) 마지막으로 받은 댓글 커서 (createdAt_commentId)
   * @return DeferredResult<CommentListResponse> - 응답이 준비되면 바로 반환, 없으면 최대 10초간 대기 후 반환
   */
  @Transactional(readOnly = true)
  public DeferredResult<CommentListResponse> pollComments(
      Long userId, Long voteId, @Nullable String cursor) {
    // Spring 비동기 응답 컨테이너
    // setResult()가 호출되면 컨트롤러가 바로 클라이언트에 응답
    DeferredResult<CommentListResponse> result = new DeferredResult<>(TIMEOUT_MILLIS);

    // 이렇게 요청마다 스레드를 만들지 않고
    // TODO: 스레드풀에 작업 제출하는 방식으로 변경
    // 비동기 작업 실행
    new Thread(
            () -> {
              try {
                // 유저, 투표, 권한 체크
                CommentPermissionContext context =
                    permissionContextFactory.validateAndGetContext(userId, voteId);
                User user = context.user();
                Vote vote = context.vote();

                CreatedAtCommentIdCursor parsedCursor =
                    cursor != null ? CreatedAtCommentIdCursor.parse(cursor) : null;

                // 롱폴링 루프
                LocalDateTime start = LocalDateTime.now();
                while (Duration.between(start, LocalDateTime.now()).toMillis() < TIMEOUT_MILLIS) {
                  // 새로운 댓글 조회
                  List<Comment> newComments =
                      commentRepository.findByVoteWithCursor(vote, parsedCursor, MAX_POLL_SIZE);

                  // 1. 새 댓글이 있으면 결과 즉시 응답
                  if (!newComments.isEmpty()) {
                    List<CommentItem> items =
                        newComments.stream().map(comment -> CommentItem.of(comment, user)).toList();

                    String nextCursor =
                        newComments.isEmpty()
                            ? cursor
                            : new CreatedAtCommentIdCursor(
                                    newComments.getLast().getCreatedAt(),
                                    newComments.getLast().getId())
                                .encode();

                    CommentListResponse response =
                        new CommentListResponse(voteId, items, nextCursor, false, items.size());
                    result.setResult(response);
                    return;
                  }
                  // 2. 새 댓글이 없으면 INTERVAL_MILLIS만큼 대기 후 재시도
                  try {
                    Thread.sleep(INTERVAL_MILLIS);
                  } catch (InterruptedException e) {
                    // 스레드 인터럽트 발생 시 로그 남기고 즉시 종료
                    log.warn(
                        "[CommentPollingService#pollComments] Polling thread interrupted: ", e);
                    Thread.currentThread().interrupt();
                    return;
                  }
                }

                // 3. 타임아웃까지 새 댓글이 없으면 빈 배열 응답
                CommentListResponse response =
                    new CommentListResponse(voteId, List.of(), cursor, false, 0);
                result.setResult(response);
              } catch (Exception e) {
                result.setErrorResult(e);
              }
            })
        .start();

    return result;
  }
}
