package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.config.CommentPollingConstants;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.global.cursor.CreatedAtCommentIdCursor;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentPollingQueryService {

  private final CommentRepository commentRepository;

  @Transactional(readOnly = true)
  public List<Comment> getNewComments(Vote vote, @Nullable CreatedAtCommentIdCursor parsedCursor) {
    return commentRepository.findByVoteWithCursorFetchUser(
        vote, parsedCursor, CommentPollingConstants.MAX_POLL_SIZE);
  }
}
