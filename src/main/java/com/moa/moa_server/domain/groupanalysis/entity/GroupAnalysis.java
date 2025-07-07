package com.moa.moa_server.domain.groupanalysis.entity;

import com.moa.moa_server.domain.group.entity.Group;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "group_analysis",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id", "week_start_at"})},
    indexes = {
      @Index(name = "idx_group_week", columnList = "group_id, week_start_at DESC"),
      @Index(name = "idx_published", columnList = "published_at")
    })
public class GroupAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Column(name = "week_start_at", nullable = false)
  private LocalDateTime weekStartAt;

  @Column(name = "generated_at", nullable = false, updatable = false)
  private LocalDateTime generatedAt;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  @PrePersist
  public void onCreate() {
    this.generatedAt = LocalDateTime.now();
  }

  public static GroupAnalysis of(
      Group group, LocalDateTime weekStartAt, LocalDateTime publishedAt) {
    return GroupAnalysis.builder()
        .group(group)
        .weekStartAt(weekStartAt)
        .publishedAt(publishedAt)
        .build();
  }
}
