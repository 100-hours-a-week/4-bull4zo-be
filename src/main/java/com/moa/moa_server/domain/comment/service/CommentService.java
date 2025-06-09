package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.dto.response.CommentCreateResponse;
import com.moa.moa_server.domain.comment.dto.response.CommentItem;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.comment.util.CommentNicknameUtil;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
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
  private final UserRepository userRepository;
  private final VoteRepository voteRepository;
  private final VoteResponseRepository voteResponseRepository;

  @Transactional
  public CommentCreateResponse createComment(
      Long userId, Long voteId, CommentCreateRequest request) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 투표 존재 확인
    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.VOTE_NOT_FOUND));

    // 댓글 작성 권한 확인 (투표 참여자(유효 응답: 1, 2)이거나 투표 등록자)
    boolean isOwner = vote.getUser().getId().equals(user.getId());
    boolean isParticipant =
        voteResponseRepository.existsByVoteIdAndUserIdAndOptionNumberIn(
            voteId, userId, List.of(1, 2));
    if (!(isOwner || isParticipant)) {
      throw new CommentException(CommentErrorCode.FORBIDDEN);
    }
    // TODO: 권한 추가 - top3 투표인 경우 모든 사용자가 댓글 작성 가능

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

    return new CommentCreateResponse(
        comment.getId(), comment.getContent(), authorNickname, comment.getCreatedAt());
  }

  @Transactional(readOnly = true)
  public CommentListResponse getComments(
      Long userId, Long voteId, @Nullable String cursor, @Nullable Integer size) {
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    CreatedAtCommentIdCursor parsedCursor =
        cursor != null ? CreatedAtCommentIdCursor.parse(cursor) : null;

    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 투표 존재 확인
    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.VOTE_NOT_FOUND));

    // 댓글 조회 권한 확인 (투표 참여자(유효 응답: 1, 2)이거나 투표 등록자)
    // TODO: 권한 추가 - top3 투표인 경우 모든 사용자가 댓글 작성 가능
    boolean isOwner = vote.getUser().getId().equals(user.getId());
    boolean isParticipant =
        voteResponseRepository.existsByVoteIdAndUserIdAndOptionNumberIn(
            voteId, userId, List.of(1, 2));
    if (!(isOwner || isParticipant)) {
      throw new CommentException(CommentErrorCode.FORBIDDEN);
    }

    // 댓글 목록 조회
    List<Comment> comments = commentRepository.findComments(vote, parsedCursor, pageSize);

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
