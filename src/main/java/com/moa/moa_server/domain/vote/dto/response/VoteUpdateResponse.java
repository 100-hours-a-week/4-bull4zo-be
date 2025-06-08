package com.moa.moa_server.domain.vote.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record VoteUpdateResponse(@Schema(description = "수정된 투표 ID", example = "123") Long voteId) {}
