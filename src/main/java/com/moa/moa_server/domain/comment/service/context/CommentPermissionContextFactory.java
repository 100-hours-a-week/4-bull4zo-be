package com.moa.moa_server.domain.comment.service.context;

import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
import com.moa.moa_server.domain.ranking.util.RankingPermissionValidator;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CommentPermissionContextFactory {

  private final UserRepository userRepository;
  private final VoteRepository voteRepository;
  private final VoteResponseRepository voteResponseRepository;
  private final RankingPermissionValidator rankingPermissionValidator;

  @Transactional(readOnly = true)
  public CommentPermissionContext validateAndGetContext(Long userId, Long voteId) {
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

    // 댓글 작성 권한 확인 (투표 참여자(유효 응답: 1, 2)이거나 투표 등록자, top3 투표)
    boolean isOwner = vote.getUser().getId().equals(user.getId());
    boolean isParticipant =
        voteResponseRepository.existsByVoteIdAndUserIdAndOptionNumberIn(
            vote.getId(), user.getId(), List.of(1, 2));
    boolean isTop3Accessible = rankingPermissionValidator.isAccessibleAsTopRankedVote(user, vote);

    if (!(isOwner || isParticipant || isTop3Accessible)) {
      throw new CommentException(CommentErrorCode.FORBIDDEN);
    }

    return new CommentPermissionContext(user, vote);
  }
}
