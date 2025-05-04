package com.moa.moa_server.domain.vote.dto.response.list;

import java.time.LocalDateTime;

public record VoteListItem(
        Long voteId,
        Long groupId,
        String authorNickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        int adminVote,
        String voteType
) {}
