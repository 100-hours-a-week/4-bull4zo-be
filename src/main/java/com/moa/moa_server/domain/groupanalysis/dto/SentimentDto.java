package com.moa.moa_server.domain.groupanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "그룹 전체 분위기 정보")
public record SentimentDto(
    @Schema(description = "감성", example = "긍정적") String emotion,
    @Schema(description = "주요 키워드", example = "[\"카테부\", \"성장\", \"점심\", \"응원\", \"특강\"]")
        List<String> topKeywords) {}
