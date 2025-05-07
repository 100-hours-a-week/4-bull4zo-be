package com.moa.moa_server.domain.user.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements BaseErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED),
    INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST),
    INVALID_NICKNAME(HttpStatus.BAD_REQUEST),
    DUPLICATED_NICKNAME(HttpStatus.CONFLICT),
    NICKNAME_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    UserErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
