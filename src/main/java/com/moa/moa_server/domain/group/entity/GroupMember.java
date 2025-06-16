package com.moa.moa_server.domain.group.entity;

import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "group_member",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"}))
@SQLDelete(sql = "UPDATE group_member SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class GroupMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id", nullable = false)
  private Group group;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private Role role = Role.MEMBER;

  @Column(name = "joined_at", nullable = false)
  private LocalDateTime joinedAt;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public enum Role {
    OWNER,
    MANAGER,
    MEMBER;

    public static Optional<Role> from(String name) {
      return Arrays.stream(values()).filter(r -> r.name().equalsIgnoreCase(name)).findFirst();
    }
  }

  public boolean isActive() {
    return this.deletedAt == null;
  }

  public static GroupMember create(User user, Group group) {
    return GroupMember.builder()
        .user(user)
        .group(group)
        .role(Role.MEMBER)
        .joinedAt(LocalDateTime.now())
        .build();
  }

  public static GroupMember createAsOwner(User user, Group group) {
    return GroupMember.builder()
        .user(user)
        .group(group)
        .role(Role.OWNER)
        .joinedAt(LocalDateTime.now())
        .build();
  }

  public void leave() {
    this.deletedAt = LocalDateTime.now();
  }

  public void rejoin() {
    this.deletedAt = null;
    this.joinedAt = LocalDateTime.now();
    this.role = Role.MEMBER;
  }

  public boolean isActiveUser() {
    return user != null && user.getUserStatus() == User.UserStatus.ACTIVE;
  }

  public void changeToOwner() {
    this.role = Role.OWNER;
  }

  public void changeRole(Role role) {
    this.role = role;
  }

  public boolean isOwnerOrManager() {
    return this.role == Role.OWNER || this.role == Role.MANAGER;
  }

  public boolean isManager() {
    return this.role == Role.MANAGER;
  }
}
