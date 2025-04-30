package com.moa.moa_server.domain.auth.handler;

import com.moa.moa_server.domain.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AuthException extends BaseException {

    private final String code;
    private final HttpStatus status;

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.name());
        this.code = errorCode.name();
        this.status = errorCode.getStatus();
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }
}
