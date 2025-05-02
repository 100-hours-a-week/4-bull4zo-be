package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "vote_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vote_id", "option_number"})
)
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

    @Column(name = "ratio", precision = 5, scale = 4, nullable = false)
    private BigDecimal ratio;

    public static VoteResult createInitial(Vote vote, int optionNumber) {
        return VoteResult.builder()
                .vote(vote)
                .optionNumber(optionNumber)
                .count(0)
                .ratio(BigDecimal.ZERO)
                .build();
    }

    public void update(int count, BigDecimal ratio) {
        if (this.count != count || this.ratio.compareTo(ratio) != 0) {
            this.count = count;
            this.ratio = ratio;
        }
    }
}
