package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.vote.entity.VoteModerationLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteModerationLogRepository extends JpaRepository<VoteModerationLog, Long> {
  Optional<VoteModerationLog> findFirstByVote_IdOrderByCreatedAtDesc(Long voteId);
}
