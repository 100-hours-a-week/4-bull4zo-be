package com.moa.moa_server.domain.vote.dto.request;

public record VoteCreateRequestDto (
        Long groupId,
        String content,
        String imageUrl,
        String closedAt,
        Boolean anonymous
) {}
