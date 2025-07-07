package com.moa.moa_server.domain.groupanalysis.mongo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB에 저장되는 그룹 분석 결과 도큐먼트
 *
 * <p>MySQL의 group_analysis 메타데이터와 매핑됨
 */
@Document(collection = "group_analysis_content")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupAnalysisContent {

  @Id private String id; // Mongo _id (자동 생성)

  @Indexed(unique = true)
  private Long analysisId; // MySQL group_analysis.id

  // 메타데이터
  private Long groupId;
  private LocalDateTime weekStartAt;
  private LocalDateTime generatedAt;
  private LocalDateTime publishedAt;

  // AI 분석 내용
  private Overview overview;
  private Sentiment sentiment;
  private List<String> modelReview;

  // 분석 생성 모델 정보
  private SourceMeta source;

  /** 투표 및 댓글 요약 정보 */
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Overview {
    private String voteSummary;
    private String commentSummary;
  }

  /** 감성 분석 결과 */
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Sentiment {
    private String emotion; // 예: 긍정적, 부정적
    private List<String> topKeywords; // 주요 키워드 리스트
  }

  /** 분석 모델 버전 정보 */
  @Getter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SourceMeta {
    private String version; // 모델 버전 (예: "1.0.0")
  }
}
