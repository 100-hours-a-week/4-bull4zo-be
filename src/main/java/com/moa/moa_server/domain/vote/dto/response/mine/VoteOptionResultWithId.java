package com.moa.moa_server.domain.vote.dto.response.mine;

public record VoteOptionResultWithId(
        Long voteId,
        int optionNumber,
        int count,
        int ratio
) {}
