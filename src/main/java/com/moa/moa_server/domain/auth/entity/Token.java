package com.moa.moa_server.domain.auth.entity;

import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private LocalDateTime expiresAt;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private LocalDateTime issuedAt;
}
