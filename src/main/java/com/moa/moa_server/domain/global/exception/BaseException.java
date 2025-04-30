package com.moa.moa_server.domain.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

    public BaseException(String message) {
        super(message);
    }

    public abstract HttpStatus getStatus();
    public abstract String getCode();
}
