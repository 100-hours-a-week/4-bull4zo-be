package com.moa.moa_server.domain.vote.dto.response;

public record VoteOptionResultWithId(
        Long voteId,
        int optionNumber,
        int count,
        int ratio
) {}
