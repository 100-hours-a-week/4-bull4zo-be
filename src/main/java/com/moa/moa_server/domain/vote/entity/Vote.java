package com.moa.moa_server.domain.vote.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

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

  @Column(length = 400, nullable = false)
  private String content;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "image_name", length = 300)
  private String imageName;

  @Column(name = "closed_at", nullable = false)
  private LocalDateTime closedAt;

  @Column(nullable = false)
  @Builder.Default
  private boolean anonymous = false;

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

  // ======= 생성자/팩토리 =======
  /** 사용자 투표 생성 팩토리 */
  public static Vote createUserVote(
      User user,
      Group group,
      String content,
      String imageUrl,
      String imageName,
      LocalDateTime closedAt,
      boolean anonymous,
      VoteStatus status,
      boolean adminVote) {
    return Vote.builder()
        .user(user)
        .group(group)
        .content(content)
        .imageUrl(imageUrl)
        .imageName(imageName)
        .closedAt(closedAt)
        .anonymous(anonymous)
        .voteStatus(status)
        .adminVote(adminVote)
        .voteType(VoteType.USER)
        .lastAnonymousNumber(0)
        .build();
  }

  // ======= 상태 변화/비즈니스 메서드 =======
  /** 투표 수정 (본문, 이미지, 종료일, 상태 PENDING으로) */
  public void updateForEdit(
      String content, String imageUrl, String imageName, LocalDateTime closedAt) {
    this.content = content;
    this.imageUrl = imageUrl;
    this.imageName = imageName;
    this.closedAt = closedAt;
    this.voteStatus = VoteStatus.PENDING; // 항상 상태 초기화
  }

  /** 검열 결과 반영 (상태만 변경) */
  public void updateModerationResult(VoteStatus voteStatus) {
    this.voteStatus = voteStatus;
  }

  /** 투표를 종료 상태로 변경 */
  public void close() {
    this.voteStatus = VoteStatus.CLOSED;
  }

  /** 마지막 익명 번호 증가 */
  public int increaseLastAnonymousNumber() {
    this.lastAnonymousNumber++;
    return this.lastAnonymousNumber;
  }

  // ======= 조회/편의 메서드 =======
  /** 투표가 OPEN 상태인지 확인 */
  public boolean isOpen() {
    return this.voteStatus == VoteStatus.OPEN;
  }

  // ======= 내부 Enum 타입 =======
  public enum VoteStatus {
    PENDING,
    REJECTED,
    OPEN,
    CLOSED
  }

  public enum VoteType {
    USER,
    AI,
    EVENT
  }
}
