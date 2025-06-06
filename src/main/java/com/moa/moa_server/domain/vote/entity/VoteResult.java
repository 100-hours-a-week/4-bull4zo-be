package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "vote_result",
    uniqueConstraints = @UniqueConstraint(columnNames = {"vote_id", "option_number"}))
public class VoteResult extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @Column(name = "option_number", nullable = false)
  private int optionNumber;

  @Column(name = "count", nullable = false)
  private int count;

  @Column(name = "ratio", precision = 7, scale = 4, nullable = false)
  private BigDecimal ratio;

  public static VoteResult create(Vote vote, int optionNumber, int count, double ratio) {
    BigDecimal roundedRatio = BigDecimal.valueOf(ratio).setScale(4, RoundingMode.HALF_UP);

    return VoteResult.builder()
        .vote(vote)
        .optionNumber(optionNumber)
        .count(count)
        .ratio(roundedRatio)
        .build();
  }
}
