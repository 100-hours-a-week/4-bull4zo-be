package com.moa.moa_server.domain.group.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum GroupErrorCode implements BaseErrorCode {
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND),;

    private final HttpStatus status;

    GroupErrorCode(HttpStatus status) { this.status = status; }

    public HttpStatus getStatus() { return status; }
}
