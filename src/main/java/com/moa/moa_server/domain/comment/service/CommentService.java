package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.dto.response.CommentCreateResponse;
import com.moa.moa_server.domain.comment.dto.response.CommentDeleteResponse;
import com.moa.moa_server.domain.comment.dto.response.CommentItem;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContext;
import com.moa.moa_server.domain.comment.service.context.CommentPermissionContextFactory;
import com.moa.moa_server.domain.comment.util.CommentNicknameUtil;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.notification.application.producer.VoteNotificationProducerImpl;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final CommentRepository commentRepository;
  private final VoteRepository voteRepository;

  private final VoteNotificationProducerImpl voteNotificationProducer;

  private final CommentPermissionContextFactory permissionContextFactory;

  /** 댓글 생성 */
  @Transactional
  public CommentCreateResponse createComment(
      Long userId, Long voteId, CommentCreateRequest request) {
    // 유저, 투표, 권한 체크
    CommentPermissionContext context =
        permissionContextFactory.validateAndGetContext(userId, voteId);
    User user = context.user();
    Vote vote = context.vote();

    // 본문 XSS 필터링
    String sanitizedContent = XssUtil.sanitize(request.content());

    // 익명 번호 할당 (한 투표 내 동일 작성자는 동일 번호)
    // TODO: race condition 테스트
    int anonymousNumber = 0;
    if (request.anonymous()) {
      anonymousNumber = getOrAssignAnonymousNumber(vote, user);
    }

    // 댓글 생성
    Comment comment = Comment.create(user, vote, sanitizedContent, anonymousNumber);

    commentRepository.save(comment);

    // authorNickname (익명1 or 닉네임)
    String authorNickname =
        CommentNicknameUtil.generateNickname(
            request.anonymous(), anonymousNumber, user.getNickname());

    voteNotificationProducer.notifyVoteCommented(voteId, userId, comment.getContent());

    return new CommentCreateResponse(
        comment.getId(), comment.getContent(), authorNickname, comment.getCreatedAt());
  }

  /** 댓글 목록 조회 */
  @Transactional(readOnly = true)
  public CommentListResponse getComments(
      Long userId, Long voteId, @Nullable String cursor, @Nullable Integer size) {
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    CreatedAtCommentIdCursor parsedCursor =
        cursor != null ? CreatedAtCommentIdCursor.parse(cursor) : null;

    // 유저, 투표, 권한 체크
    CommentPermissionContext context =
        permissionContextFactory.validateAndGetContext(userId, voteId);
    User user = context.user();
    Vote vote = context.vote();

    // 댓글 목록 조회
    List<Comment> comments = commentRepository.findByVoteWithCursor(vote, parsedCursor, pageSize);

    // 응답 구성 (nextCursor, hasNext 계산)
    boolean hasNext = comments.size() > pageSize;
    if (hasNext) comments = comments.subList(0, pageSize);

    String nextCursor =
        hasNext
            ? new CreatedAtCommentIdCursor(
                    comments.getLast().getCreatedAt(), comments.getLast().getId())
                .encode()
            : null;

    List<CommentItem> items =
        comments.stream().map(comment -> CommentItem.of(comment, user)).toList();

    // 응답
    return new CommentListResponse(voteId, items, nextCursor, hasNext, items.size());
  }

  /** 댓글 삭제 */
  @Transactional
  public CommentDeleteResponse deleteComment(Long userId, Long commentId) {
    Comment comment =
        commentRepository
            .findById(commentId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

    if (!comment.getUser().getId().equals(userId)) {
      throw new CommentException(CommentErrorCode.FORBIDDEN);
    }

    if (!comment.isDeleted()) {
      comment.softDelete();
    }

    return new CommentDeleteResponse(commentId);
  }

  /** 댓글 테이블에서 익명 번호 조회 및 할당 */
  private int getOrAssignAnonymousNumber(Vote vote, User user) {
    // 기존에 달았던 익명 댓글이 있다면 같은 번호, 없다면 새 번호
    Integer existNumber =
        commentRepository.findFirstAnonymousNumberByVoteIdAndUserId(vote.getId(), user.getId());
    if (existNumber != null && existNumber > 0) return existNumber;

    // 새 번호 할당
    int nextNumber = vote.increaseLastAnonymousNumber();
    voteRepository.save(vote);
    return nextNumber;
  }
}
