package com.moa.moa_server.domain.vote.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 삭제 응답 DTO")
public record VoteDeleteResponse(@Schema(description = "삭제된 투표 ID", example = "123") Long voteId) {}
