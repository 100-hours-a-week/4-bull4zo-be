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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentPollingAsyncService {

  private final CommentRepository commentRepository;
  private final CommentPermissionContextFactory permissionContextFactory;

  @Async("commentPollingExecutor")
  @Transactional(readOnly = true)
  public void pollAsync(
      Long userId,
      Long voteId,
      @Nullable String cursor,
      long timeoutMillis,
      int intervalMillis,
      int maxPollSize,
      DeferredResult<CommentListResponse> result) {

    try {
      // 유저, 투표, 권한 체크
      CommentPermissionContext context =
          permissionContextFactory.validateAndGetContext(userId, voteId);
      User user = context.user();
      Vote vote = context.vote();

      // 커서 파싱
      CreatedAtCommentIdCursor parsedCursor =
          cursor != null ? CreatedAtCommentIdCursor.parse(cursor) : null;

      // 롱폴링 루프: TIMEOUT_MILLIS 까지 INTERVAL_MILLIS 간격으로 새로운 댓글을 조회한다.
      LocalDateTime start = LocalDateTime.now();
      log.debug(
          "[CommentPollingService#pollComments] 롱폴링 시작 - userId={}, voteId={}, cursor={}",
          userId,
          voteId,
          cursor);
      while (Duration.between(start, LocalDateTime.now()).toMillis() < timeoutMillis) {
        // 새로운 댓글 조회
        List<Comment> newComments =
            commentRepository.findByVoteWithCursor(vote, parsedCursor, maxPollSize);

        // 1. 새 댓글이 있으면 결과 즉시 응답 및 종료
        if (!newComments.isEmpty()) {
          List<CommentItem> items =
              newComments.stream().map(comment -> CommentItem.of(comment, user)).toList();

          String nextCursor =
              newComments.isEmpty()
                  ? cursor
                  : new CreatedAtCommentIdCursor(
                          newComments.getLast().getCreatedAt(), newComments.getLast().getId())
                      .encode();

          log.debug(
              "[CommentPollingService#pollComments] 롱폴링 종료 - 새 댓글 감지, userId={}, voteId={}, count={}",
              userId,
              voteId,
              items.size());
          result.setResult(new CommentListResponse(voteId, items, nextCursor, false, items.size()));
          return;
        }

        // 2. 새 댓글이 없으면 INTERVAL_MILLIS 만큼 대기 후 재조회
        try {
          Thread.sleep(intervalMillis);
        } catch (InterruptedException e) {
          // 스레드 인터럽트 발생 시 로그 남기고 즉시 종료
          log.warn(
              "[CommentPollingService#pollComments] 롱폴링 스레드 인터럽트 발생: userId={}, voteId={}",
              userId,
              voteId,
              e);
          Thread.currentThread().interrupt();
          break;
        }
      }

      // 3. 타임아웃까지 새 댓글이 없으면 빈 배열 응답
      log.debug(
          "[CommentPollingService#pollComments] 롱폴링 종료 - 타임아웃, userId={}, voteId={}",
          userId,
          voteId);
      result.setResult(new CommentListResponse(voteId, List.of(), cursor, false, 0));
    } catch (Exception e) {
      log.error("[CommentPollingService#pollComments] 롱폴링 중 예외 발생", e);
      result.setErrorResult(e);
    }
  }
}
