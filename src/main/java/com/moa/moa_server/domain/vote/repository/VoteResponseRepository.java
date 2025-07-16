package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
  boolean existsByVoteAndUser(Vote vote, User user);

  Optional<VoteResponse> findByVoteAndUser(Vote vote, User user);

  List<VoteResponse> findAllByVote(Vote vote);

  boolean existsByVoteIdAndUserIdAndOptionNumberIn(Long voteId, Long userId, List<Integer> options);

  @Query(
      """
  SELECT COUNT(DISTINCT vr.user.id)
  FROM VoteResponse vr
  WHERE vr.vote.group.id = :groupId
    AND vr.votedAt BETWEEN :start AND :end
""")
  int countDistinctUserIdsByGroupAndPeriod(
      @Param("groupId") Long groupId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  int countByVoteId(Long voteId);
}
