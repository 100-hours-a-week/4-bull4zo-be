package com.moa.moa_server.domain.vote.dto.request;

import java.time.LocalDateTime;

public record VoteCreateRequest(
        Long groupId,
        String content,
        String imageUrl,
        LocalDateTime closedAt,
        Boolean anonymous
) {}
