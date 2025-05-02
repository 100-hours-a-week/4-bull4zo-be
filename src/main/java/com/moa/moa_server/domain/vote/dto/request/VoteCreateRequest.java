package com.moa.moa_server.domain.vote.dto.request;

public record VoteCreateRequest(
        Long groupId,
        String content,
        String imageUrl,
        String closedAt,
        Boolean anonymous
) {}
