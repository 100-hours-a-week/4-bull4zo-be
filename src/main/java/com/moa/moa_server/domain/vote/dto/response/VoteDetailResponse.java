package com.moa.moa_server.domain.vote.dto.response;

import java.time.LocalDateTime;

public record VoteDetailResponse(
        Long voteId,
        Long groupId,
        String authorNickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime closedAt
) {}
