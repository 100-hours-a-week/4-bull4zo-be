package com.moa.moa_server.domain.ranking.repository;

import com.moa.moa_server.domain.ranking.entity.VoteRanking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRankingRepository extends JpaRepository<VoteRanking, Long> {}
