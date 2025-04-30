package com.moa.moa_server.domain.user.entity;

import com.moa.moa_server.domain.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(length = 255, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role; // USER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", length = 20, nullable = false)
    private UserStatus userStatus;

    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawn_at;

    public enum Role {
        USER,
        ADMIN
    }

    public enum UserStatus {
        ACTIVE,
        WITHDRAWN,
        DORMANT,
    }
}