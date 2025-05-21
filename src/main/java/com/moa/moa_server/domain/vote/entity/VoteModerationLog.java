package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "vote_moderation_log")
public class VoteModerationLog extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_result", length = 20, nullable = false)
  private ReviewResult reviewResult;

  @Enumerated(EnumType.STRING)
  @Column(name = "review_reason", length = 50, nullable = false)
  private ReviewReason reviewReason;

  @Column(name = "review_detail", length = 500, nullable = false)
  private String reviewDetail;

  @Column(name = "ai_version", length = 50, nullable = false)
  private String aiVersion;

  public enum ReviewResult {
    REJECTED,
    APPROVED
  }

  public enum ReviewReason {
    NONE,
    OFFENSIVE_LANGUAGE,
    POLITICAL_CONTENT,
    SEXUAL_CONTENT,
    SPAM_ADVERTISEMENT,
    IMPERSONATION_OR_LEAK,
    OTHER
  }

  public static VoteModerationLog create(
      Vote vote,
      ReviewResult reviewResult,
      ReviewReason reviewReason,
      String reviewDetail,
      String aiVersion) {
    return VoteModerationLog.builder()
        .vote(vote)
        .reviewResult(reviewResult)
        .reviewReason(reviewReason)
        .reviewDetail(reviewDetail)
        .aiVersion(aiVersion)
        .build();
  }
}
