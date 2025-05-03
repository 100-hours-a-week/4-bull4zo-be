package com.moa.moa_server.domain.auth.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class AuthException extends BaseException {
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
