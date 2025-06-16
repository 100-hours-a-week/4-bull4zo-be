package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class VoteRelatedDataCleaner {

  private final CommentRepository commentRepository;

  @Transactional
  public void cleanup(Long voteId) {
    commentRepository.softDeleteByVoteId(voteId);
  }
}
