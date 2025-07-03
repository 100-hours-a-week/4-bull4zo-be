package com.moa.moa_server.domain.comment.service;

import com.moa.moa_server.domain.comment.repository.CommentRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentCountService {

  private final CommentRepository commentRepository;

  public Map<Long, Integer> getCommentCountsByVoteIds(List<Long> voteIds) {
    List<Object[]> rawCounts = commentRepository.countCommentsByVoteIds(voteIds);
    return rawCounts.stream()
        .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Long) row[1]).intValue()));
  }
}
