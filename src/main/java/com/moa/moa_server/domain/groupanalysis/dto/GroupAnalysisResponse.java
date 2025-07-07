package com.moa.moa_server.domain.groupanalysis.dto;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.groupanalysis.entity.GroupAnalysis;
import com.moa.moa_server.domain.groupanalysis.mongo.GroupAnalysisContent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "그룹 분석 조회 응답 DTO")
public record GroupAnalysisResponse(
    @Schema(description = "그룹 ID", example = "123") Long groupId,
    @Schema(description = "그룹 이름", example = "카테부") String groupName,
    @Schema(description = "분석 대상 주차의 시작 시각", example = "2025-06-30T00:00:00")
        LocalDateTime weekStartAt,
    @Schema(description = "참여 통계") ParticipationStats participationStats,
    @Schema(description = "분석 내용") AnalysisContent analysis) {

  @Schema(description = "참여 통계")
  public record ParticipationStats(
      @Schema(description = "참여자 비율", example = "70.0") double participated,
      @Schema(description = "미참여자 비율", example = "30.0") double notParticipated) {}

  @Schema(description = "분석 내용")
  public record AnalysisContent(
      @Schema(description = "투표 및 댓글 내용 요약") OverviewDto overview,
      @Schema(description = "그룹 전체 분위기 정보") SentimentDto sentiment,
      @Schema(description = "모델이 생성한 총평 문장 리스트") List<String> modelReview) {}

  public static GroupAnalysisResponse empty(
      Group group, LocalDateTime weekStartAt, ParticipationStats stats) {
    return new GroupAnalysisResponse(group.getId(), group.getName(), weekStartAt, stats, null);
  }

  public static GroupAnalysisResponse from(
      Group group,
      GroupAnalysis metadata,
      GroupAnalysisContent analysisContent,
      ParticipationStats stats) {
    return new GroupAnalysisResponse(
        group.getId(),
        group.getName(),
        metadata.getWeekStartAt(),
        stats,
        new AnalysisContent(
            new OverviewDto(
                analysisContent.getOverview().getVoteSummary(),
                analysisContent.getOverview().getCommentSummary()),
            new SentimentDto(
                analysisContent.getSentiment().getEmotion(),
                analysisContent.getSentiment().getTopKeywords()),
            analysisContent.getModelReview()));
  }
}
