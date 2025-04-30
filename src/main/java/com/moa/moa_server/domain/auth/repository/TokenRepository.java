package com.moa.moa_server.domain.auth.repository;

import com.moa.moa_server.domain.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByRefreshToken(String refreshToken);
}
