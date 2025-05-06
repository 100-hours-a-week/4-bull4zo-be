package com.moa.moa_server.domain.group.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum GroupErrorCode implements BaseErrorCode {
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND),
    INVALID_CODE_FORMAT(HttpStatus.BAD_REQUEST),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND),
    ALREADY_JOINED(HttpStatus.CONFLICT),
    CANNOT_JOIN_PUBLIC_GROUP(HttpStatus.BAD_REQUEST),;

    private final HttpStatus status;

    GroupErrorCode(HttpStatus status) { this.status = status; }

    public HttpStatus getStatus() { return status; }
}
