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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;

@Service
@RequiredArgsConstructor
public class CommentPollingService {

  private final long TIMEOUT_MILLIS = 10000L; // 10초 timeout
  private final int INTERVAL_MILLIS = 500; // 5초 폴링 주기
  private final int MAX_POLL_SIZE = 10; // 한 번에 받아올 최대 댓글 수

  private final CommentRepository commentRepository;

  private final CommentPermissionContextFactory permissionContextFactory;

  @Transactional(readOnly = true)
  public DeferredResult<CommentListResponse> pollComments(
      Long userId, Long voteId, @Nullable String cursor) {
    DeferredResult<CommentListResponse> result = new DeferredResult<>(TIMEOUT_MILLIS);

    // 이렇게 요청마다 스레드를 만들지 않고
    // TODO: 스레드풀에 작업 제출하는 방식으로 변경
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

                // polling
                LocalDateTime start = LocalDateTime.now();
                while (Duration.between(start, LocalDateTime.now()).toMillis() < TIMEOUT_MILLIS) {
                  List<Comment> newComments =
                      commentRepository.findByVoteWithCursor(vote, parsedCursor, MAX_POLL_SIZE);
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
                  try {
                    Thread.sleep(INTERVAL_MILLIS);
                  } catch (InterruptedException ignored) {
                  }
                }

                // TIMEOUT까지 새 댓글이 없다면 빈 배열 반환, nextCursor는 그대로(또는 최신 커서)
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
