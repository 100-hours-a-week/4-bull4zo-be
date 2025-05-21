package com.moa.moa_server.domain.global.exception;

public class GlobalException extends BaseException {
  public GlobalException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
