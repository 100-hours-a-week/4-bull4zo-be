package com.moa.moa_server.domain.vote.dto.response.mine;

import java.util.List;

public record MyVoteResponse(
    List<MyVoteItem> votes,
    String nextCursor,
    boolean hasNext,
    int size
) {
    public static MyVoteResponse of(List<MyVoteItem> votes, String nextCursor, boolean hasNext) {
        return new MyVoteResponse(votes, nextCursor, hasNext, votes.size());
    }
}
