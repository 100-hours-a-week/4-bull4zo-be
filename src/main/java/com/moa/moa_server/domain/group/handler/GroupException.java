package com.moa.moa_server.domain.group.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class GroupException extends BaseException {
  public GroupException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
