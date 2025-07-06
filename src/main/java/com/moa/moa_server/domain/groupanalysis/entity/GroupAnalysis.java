package com.moa.moa_server.domain.groupanalysis.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "group_analysis")
public class GroupAnalysis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "group_id", nullable = false)
  private Long groupId;

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
}
