package com.moa.moa_server.domain.user.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class UserException extends BaseException {
    public UserException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
