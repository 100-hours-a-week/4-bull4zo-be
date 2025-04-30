package com.moa.moa_server.domain.auth.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode {

    NO_TOKEN(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST),
    KAKAO_TOKEN_FAILED(HttpStatus.UNAUTHORIZED),
    KAKAO_USERINFO_FAILED(HttpStatus.UNAUTHORIZED);

    private final HttpStatus status;

    AuthErrorCode(HttpStatus status) {
        this.status = status;
    }
}
