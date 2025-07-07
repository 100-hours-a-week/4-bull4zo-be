package com.moa.moa_server.domain.groupanalysis.mapper;

import com.moa.moa_server.domain.groupanalysis.dto.AIGroupAnalysisCreateRequest;
import com.moa.moa_server.domain.groupanalysis.entity.GroupAnalysis;
import com.moa.moa_server.domain.groupanalysis.mongo.GroupAnalysisContent;

public class GroupAnalysisContentMapper {
  public static GroupAnalysisContent toDocument(
      GroupAnalysis metadata, AIGroupAnalysisCreateRequest analysisContent) {
    return GroupAnalysisContent.builder()
        .analysisId(metadata.getId())
        .groupId(metadata.getGroup().getId())
        .weekStartAt(metadata.getWeekStartAt())
        .generatedAt(metadata.getGeneratedAt())
        .publishedAt(metadata.getPublishedAt())
        .overview(
            new GroupAnalysisContent.Overview(
                analysisContent.overview().voteSummary(),
                analysisContent.overview().commentSummary()))
        .sentiment(
            new GroupAnalysisContent.Sentiment(
                analysisContent.sentiment().emotion(), analysisContent.sentiment().topKeywords()))
        .modelReview(analysisContent.modelReview())
        .source(new GroupAnalysisContent.SourceMeta(analysisContent.version()))
        .build();
  }
}
