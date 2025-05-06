package com.moa.moa_server.domain.vote.dto.response.submitted;

import java.util.List;

public record SubmittedVoteResponse(
        List<SubmittedVoteItem> votes,
        String nextCursor,
        boolean hasNext,
        int size
) {
    public static SubmittedVoteResponse of(List<SubmittedVoteItem> votes, String nextCursor, boolean hasNext) {
        return new SubmittedVoteResponse(votes, nextCursor, hasNext, votes.size());
    }
}
