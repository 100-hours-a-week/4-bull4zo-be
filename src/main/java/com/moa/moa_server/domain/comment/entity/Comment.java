package com.moa.moa_server.domain.comment.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "comment")
public class Comment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "vote_id", nullable = false)
  private Vote vote;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(length = 500, nullable = false)
  private String content;

  @Column(name = "anonymous_number", nullable = false)
  @Builder.Default
  private int anonymousNumber = 0;

  @Column(name = "report_count", nullable = false)
  @Builder.Default
  private int reportCount = 0;

  @Column(name = "hidden_by_report", nullable = false)
  @Builder.Default
  private boolean hiddenByReport = false;

  @Column(name = "hidden_by_admin", nullable = false)
  @Builder.Default
  private boolean hiddenByAdmin = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
