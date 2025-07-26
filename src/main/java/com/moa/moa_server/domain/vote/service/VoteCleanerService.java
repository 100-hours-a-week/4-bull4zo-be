package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteCleanerService {

  private final VoteRepository voteRepository;
  private final CommentRepository commentRepository;

  public void deleteVoteByGroupId(Long groupId) {
    // 1. 해당 그룹의 모든 투표 soft delete
    voteRepository.softDeleteByGroupId(groupId);

    // 2. 각 투표의 연관 데이터 정리
    List<Long> voteIds = voteRepository.findAllIdsByGroupId(groupId);
    for (Long voteId : voteIds) {
      cleanup(voteId);
    }
  }

  void cleanup(Long voteId) {
    commentRepository.softDeleteByVoteId(voteId);
  }
}
