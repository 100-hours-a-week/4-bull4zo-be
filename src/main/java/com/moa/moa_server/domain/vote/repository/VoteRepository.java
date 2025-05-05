package com.moa.moa_server.domain.vote.repository;

import com.moa.moa_server.domain.vote.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long>, VoteRepositoryCustom {
}
