package com.moa.moa_server.domain.notification.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "notification")
public class Notification extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(length = 50, nullable = false)
  private NotificationType type;

  @Column(length = 255, nullable = false)
  private String content;

  @Column(name = "isRead", nullable = false)
  @Builder.Default
  private boolean isRead = false;

  @Column(name = "redirect_url", length = 255)
  private String redirectUrl;

  public void markAsRead() {
    this.isRead = true;
  }
}
