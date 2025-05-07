package com.moa.moa_server.domain.auth.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements BaseErrorCode {

    NO_TOKEN(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST),
    KAKAO_TOKEN_FAILED(HttpStatus.UNAUTHORIZED),
    KAKAO_USERINFO_FAILED(HttpStatus.UNAUTHORIZED),
    OAUTH_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_REDIRECT_URI(HttpStatus.BAD_REQUEST),;

    private final HttpStatus status;

    AuthErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
