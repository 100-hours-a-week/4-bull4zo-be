package com.moa.moa_server.domain.vote.dto.response.list;

import java.util.List;

public record VoteListResponse(
        List<VoteListItem> votes,
        String nextCursor,
        boolean hasNext,
        int size
) {}
