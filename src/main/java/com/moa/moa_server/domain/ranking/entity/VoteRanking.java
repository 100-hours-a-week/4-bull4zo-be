package com.moa.moa_server.domain.ranking.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "vote_ranking",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "ranked_at", "rank"}))
public class VoteRanking extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "vote_id", nullable = false)
  private Long voteId;

  @Column(name = "group_id", nullable = false)
  private Long groupId;

  @Column(nullable = false)
  private int rank;

  @Column(name = "ranked_at", nullable = false)
  private LocalDateTime rankedAt;

  public static VoteRanking of(Long voteId, Long groupId, int rank, LocalDateTime rankedAt) {
    return VoteRanking.builder()
        .voteId(voteId)
        .groupId(groupId)
        .rank(rank)
        .rankedAt(rankedAt)
        .build();
  }
}
