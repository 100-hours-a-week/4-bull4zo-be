package com.moa.moa_server.domain.group.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
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
@Table(name = "`group`", uniqueConstraints = @UniqueConstraint(columnNames = "invite_code"))
@SQLDelete(sql = "UPDATE `group` SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Group extends BaseTimeEntity {

  public static final Long PUBLIC_GROUP_ID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "name", length = 50, nullable = false, unique = true)
  private String name;

  @Column(name = "description", length = 300, nullable = false)
  private String description;

  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "image_name", length = 300)
  private String imageName;

  @Column(name = "invite_code", length = 20, nullable = false, unique = true)
  private String inviteCode;

  @Column(name = "deleted_at")
  private java.time.LocalDateTime deletedAt;

  public static Group create(
      User user,
      String name,
      String description,
      String imageUrl,
      String imageName,
      String inviteCode) {
    return Group.builder()
        .user(user)
        .name(name)
        .description(description)
        .imageUrl(imageUrl)
        .imageName(imageName)
        .inviteCode(inviteCode)
        .build();
  }

  public void softDelete() {
    this.deletedAt = LocalDateTime.now();
  }

  public void changeOwner(User newOwner) {
    this.user = newOwner;
  }

  public boolean isPublicGroup() {
    return this.id != null && this.id.equals(PUBLIC_GROUP_ID);
  }

  public boolean isOwnedBy(long userId) {
    return this.user.getId().equals(userId);
  }
}
