package com.moa.moa_server.domain.groupanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "투표 및 댓글 내용 요약")
public record OverviewDto(
    @Schema(description = "투표 요약", example = "그룹원들은 주로 카떼부에 대한 다양한 의견과 경험을 공유했습니다.")
        String voteSummary,
    @Schema(description = "댓글 요약", example = "댓글들은 대체로 긍정적이며, 서로를 격려하고 응원하는 내용이 많았습니다.")
        String commentSummary) {}
