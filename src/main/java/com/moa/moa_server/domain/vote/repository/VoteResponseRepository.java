package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.vote.entity.VoteResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteResponseRepository extends JpaRepository<VoteResponse, Long> {
}