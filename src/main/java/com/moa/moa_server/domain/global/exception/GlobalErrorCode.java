package com.moa.moa_server.domain.global.exception;

import org.springframework.http.HttpStatus;

public enum GlobalErrorCode implements BaseErrorCode{

    FORBIDDEN(HttpStatus.FORBIDDEN),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),;

    private final HttpStatus status;

    GlobalErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
