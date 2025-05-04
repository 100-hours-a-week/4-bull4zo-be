package com.moa.moa_server.domain.vote.dto.response.result;

public record VoteOptionResult(
        int optionNumber,
        int count,
        int ratio
) {}