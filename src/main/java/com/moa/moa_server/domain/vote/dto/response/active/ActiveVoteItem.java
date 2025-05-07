package com.moa.moa_server.domain.vote.dto.response.active;

import com.moa.moa_server.domain.vote.entity.Vote;

import java.time.LocalDateTime;

public record ActiveVoteItem(
        Long voteId,
        Long groupId,
        String authorNickname,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        int adminVote,
        String voteType
) {

    public static ActiveVoteItem from(Vote vote) {
        return new ActiveVoteItem(
                vote.getId(),
                vote.getGroup().getId(),
                vote.isAnonymous() ? "익명" : vote.getUser().getNickname(),
                vote.getContent(),
                vote.getImageUrl(),
                vote.getCreatedAt(),
                vote.getClosedAt(),
                vote.isAdminVote() ? 1 : 0,
                vote.getVoteType().name()
        );
    }
}
