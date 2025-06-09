package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.dto.response.CommentCreateResponse;
import com.moa.moa_server.domain.comment.dto.response.CommentListResponse;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

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
        getAuthorNickname(request.anonymous(), anonymousNumber, user.getNickname());

    return new CommentCreateResponse(
        comment.getId(), comment.getContent(), authorNickname, comment.getCreatedAt());
  }

  public CommentListResponse getComments(Long userId, Long voteId) {
    return null;
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

  /** 댓글 작성자 닉네임 생성 */
  private String getAuthorNickname(boolean anonymous, int anonymousNumber, String nickname) {
    return anonymous ? "익명" + anonymousNumber : nickname;
  }
}
