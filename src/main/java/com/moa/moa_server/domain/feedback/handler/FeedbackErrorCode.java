package com.moa.moa_server.domain.feedback.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum FeedbackErrorCode implements BaseErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST);

    private final HttpStatus status;

    FeedbackErrorCode(HttpStatus status) { this.status = status; }

    public HttpStatus getStatus() { return status; }

}
