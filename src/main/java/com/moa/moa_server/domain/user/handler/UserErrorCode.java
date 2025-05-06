package com.moa.moa_server.domain.user.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED),
    INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST),;

    private final HttpStatus status;

    UserErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
