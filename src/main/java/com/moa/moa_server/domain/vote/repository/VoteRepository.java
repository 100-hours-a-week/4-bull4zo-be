package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoteRepository extends JpaRepository<Vote, Long>, VoteRepositoryCustom {

  @Modifying
  @Query("UPDATE Vote v SET v.deletedAt = CURRENT_TIMESTAMP WHERE v.user = :user")
  void softDeleteAllByUser(@Param("user") User user);

  @Modifying(clearAutomatically = true)
  @Query(
      """
    UPDATE Vote v
    SET v.voteStatus = 'OPEN'
    WHERE v.voteStatus = 'PENDING'
      AND v.voteType = 'AI'
      AND v.openAt <= :now
""")
  int updateOpenStatusForAIVotes(@Param("now") LocalDateTime now);
}
