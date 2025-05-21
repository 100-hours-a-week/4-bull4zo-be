package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
  boolean existsByVoteAndUser(Vote vote, User user);

  Optional<VoteResponse> findByVoteAndUser(Vote vote, User user);

  List<VoteResponse> findAllByVote(Vote vote);
}
