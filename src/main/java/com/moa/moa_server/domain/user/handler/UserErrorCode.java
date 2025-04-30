package com.moa.moa_server.domain.user.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_WITHDRAWN(HttpStatus.UNAUTHORIZED);

    private final HttpStatus status;

    UserErrorCode(HttpStatus status) {
        this.status = status;
    }
}
