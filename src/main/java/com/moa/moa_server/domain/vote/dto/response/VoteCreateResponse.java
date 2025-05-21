package com.moa.moa_server.domain.vote.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 등록 응답 DTO")
public record VoteCreateResponse(@Schema(description = "등록된 투표 ID", example = "123") Long voteId) {}
