package com.moa.moa_server.domain.vote.dto.response.active;

import java.util.List;

public record ActiveVoteResponse(
        List<ActiveVoteItem> votes,
        String nextCursor,
        boolean hasNext,
        int size
) {}
