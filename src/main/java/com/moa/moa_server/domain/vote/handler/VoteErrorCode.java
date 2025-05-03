package com.moa.moa_server.domain.vote.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VoteErrorCode {

    INVALID_CONTENT(HttpStatus.BAD_REQUEST),
    INVALID_URL(HttpStatus.BAD_REQUEST),
    INVALID_TIME(HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND),
    NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN);

    private final HttpStatus status;

    VoteErrorCode(HttpStatus status) {
        this.status = status;
    }
}
