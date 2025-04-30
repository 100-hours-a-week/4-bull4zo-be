package com.moa.moa_server.domain.user.handler;

import com.moa.moa_server.domain.global.exception.BaseException;
import org.springframework.http.HttpStatus;

public class UserException extends BaseException {

    private final String code;
    private final HttpStatus status;

    public UserException(UserErrorCode errorCode) {
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
