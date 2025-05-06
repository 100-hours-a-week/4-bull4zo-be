package com.moa.moa_server.domain.vote.dto.response.submitted;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.entity.Vote;

import java.time.LocalDateTime;
import java.util.List;

public record SubmittedVoteItem(
        Long voteId,
        Long groupId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        List<VoteOptionResultWithId> results
) {
    public static SubmittedVoteItem from(Vote vote, List<VoteOptionResultWithId> results) {
        return new SubmittedVoteItem(
                vote.getId(),
                vote.getGroup().getId(),
                vote.getContent(),
                vote.getCreatedAt(),
                vote.getClosedAt(),
                results
        );
    }
}
