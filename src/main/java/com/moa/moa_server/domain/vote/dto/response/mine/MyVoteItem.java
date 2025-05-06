package com.moa.moa_server.domain.vote.dto.response.mine;

import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;

import java.time.LocalDateTime;
import java.util.List;

public record MyVoteItem(
        Long voteId,
        Long groupId,
        String content,
        String voteStatus,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        List<VoteOptionResultWithId> results
) {

    public static MyVoteItem from(Vote vote, List<VoteOptionResultWithId> results) {
        return new MyVoteItem(
                vote.getId(),
                vote.getGroup().getId(),
                vote.getContent(),
                vote.getVoteStatus().name(),
                vote.getCreatedAt(),
                vote.getClosedAt(),
                results
        );
    }
}
