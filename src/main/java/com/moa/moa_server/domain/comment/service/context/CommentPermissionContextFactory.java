package com.moa.moa_server.domain.comment.service.context;

import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.handler.CommentException;
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

@Component
@RequiredArgsConstructor
public class CommentPermissionContextFactory {

  private final UserRepository userRepository;
  private final VoteRepository voteRepository;
  private final VoteResponseRepository voteResponseRepository;

  public CommentPermissionContext validateAndGetContext(Long userId, Long voteId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new CommentException(CommentErrorCode.VOTE_NOT_FOUND));

    boolean isOwner = vote.getUser().getId().equals(user.getId());
    boolean isParticipant =
        voteResponseRepository.existsByVoteIdAndUserIdAndOptionNumberIn(
            vote.getId(), user.getId(), List.of(1, 2));
    if (!(isOwner || isParticipant)) {
      throw new CommentException(CommentErrorCode.FORBIDDEN);
    }
    return new CommentPermissionContext(user, vote);
  }
}
