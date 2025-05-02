package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "vote")
@SQLDelete(sql = "UPDATE vote SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Vote extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(length = 500, nullable = false)
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "closed_at", nullable = false)
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_status", nullable = false, length = 20)
    @Builder.Default
    private VoteStatus voteStatus = VoteStatus.PENDING;

    @Column(name = "admin_vote", nullable = false)
    @Builder.Default
    private boolean adminVote = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_type", nullable = false, length = 20)
    @Builder.Default
    private VoteType voteType = VoteType.USER;

    @Column(name = "last_anonymous_number", nullable = false)
    @Builder.Default
    private int lastAnonymousNumber = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public enum VoteStatus {
        PENDING, REJECTED, OPEN, CLOSED
    }

    public enum VoteType {
        USER, AI, EVENT
    }
}
