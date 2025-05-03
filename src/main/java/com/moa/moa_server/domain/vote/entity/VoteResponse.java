package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "vote_response",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "vote_id"})
)
public class VoteResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vote_id", nullable = false)
    private Vote vote;

    @Column(name = "option_number", nullable = false)
    private int optionNumber;

    @CreatedDate
    @Column(name = "voted_at", nullable = false)
    private LocalDateTime votedAt;

    public static VoteResponse create(Vote vote, User user, int optionNumber) {
        return VoteResponse.builder()
                .vote(vote)
                .user(user)
                .optionNumber(optionNumber)
                .build();
    }
}
