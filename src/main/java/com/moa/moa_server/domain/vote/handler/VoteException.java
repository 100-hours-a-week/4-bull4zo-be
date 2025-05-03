package com.moa.moa_server.domain.vote.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class VoteException extends BaseException {
    public VoteException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
