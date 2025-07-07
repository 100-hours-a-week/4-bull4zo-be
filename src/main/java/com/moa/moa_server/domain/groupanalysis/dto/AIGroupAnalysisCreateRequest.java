package com.moa.moa_server.domain.groupanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "그룹 분석 저장 요청 DTO")
public record AIGroupAnalysisCreateRequest(
    @Schema(description = "분석 대상 그룹 ID", example = "123") Long groupId,
    @Schema(description = "분석 대상 주차의 시작 시각", example = "2025-06-30T00:00:00")
        LocalDateTime weekStartAt,
    @Schema(description = "투표 및 댓글 내용 요약") OverviewDto overview,
    @Schema(description = "그룹 전체 분위기 정보") SentimentDto sentiment,
    @Schema(description = "모델이 생성한 총평 문장 리스트", example = "[\"참여율이 높았습니다\", \"긍정적인 피드백이 많았습니다\"]")
        List<String> modelReview,
    @Schema(description = "AI 분석 모델 버전", example = "1.0.0") String version) {}
