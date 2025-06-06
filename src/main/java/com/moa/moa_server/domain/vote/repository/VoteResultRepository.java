package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.vote.entity.VoteResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteResultRepository extends JpaRepository<VoteResult, Long> {

  @Modifying
  @Query(
      """
    UPDATE VoteResult v
    SET v.count = v.count + 1
    WHERE v.vote.id = :voteId AND v.optionNumber = :optionNumber
  """)
  int incrementOptionCount(@Param("voteId") Long voteId, @Param("optionNumber") int optionNumber);

  List<VoteResult> findAllByVoteId(Long voteId);

  boolean existsByVoteId(Long voteId);
}
