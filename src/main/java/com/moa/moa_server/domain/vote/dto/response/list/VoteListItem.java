package com.moa.moa_server.domain.vote.dto.response.list;

import com.moa.moa_server.domain.vote.entity.Vote;

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
) {

    public static VoteListItem from(Vote vote) {
        return new VoteListItem(
                vote.getId(),
                vote.getGroup().getId(),
                vote.getUser().getNickname(),
                vote.getContent(),
                vote.getImageUrl(),
                vote.getCreatedAt(),
                vote.getClosedAt(),
                vote.isAdminVote() ? 1 : 0,
                vote.getVoteType().name()
        );
    }
}
