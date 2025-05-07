package com.moa.moa_server.domain.vote.dto.moderation;

public record VoteModerationCallbackRequest(
        Long voteId,
        String result,
        String reason,
        String reasonDetail,
        String version
) {}
