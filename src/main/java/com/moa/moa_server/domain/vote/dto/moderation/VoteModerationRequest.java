package com.moa.moa_server.domain.vote.dto.moderation;

public record VoteModerationRequest(
        Long voteId,
        String content
) {}