package com.moa.moa_server.domain.vote.dto.moderation;

public record VoteModerationCallbackResponse(
        Long voteId,
        boolean stored
) {}
