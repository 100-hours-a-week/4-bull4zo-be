package com.moa.moa_server.domain.vote.model;

import com.moa.moa_server.domain.vote.entity.Vote;
import java.time.LocalDateTime;

public record VoteWithVotedAt(Vote vote, LocalDateTime votedAt) {}
