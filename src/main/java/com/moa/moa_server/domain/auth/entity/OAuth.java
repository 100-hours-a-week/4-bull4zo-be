package com.moa.moa_server.domain.auth.entity;

import com.moa.moa_server.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;

@Entity
@Table(name = "oauth", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "provider_code"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class OAuth {

    @Id
    @Column(length = 100, nullable = false)
    private String id; // 소셜 사용자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_code", length = 20, nullable = false)
    private ProviderCode providerCode;

    public enum ProviderCode {
        KAKAO;

        public static boolean isSupported(String provider) {
            return Arrays.stream(values())
                    .anyMatch(p -> p.name().equalsIgnoreCase(provider));
        }
    }
}
