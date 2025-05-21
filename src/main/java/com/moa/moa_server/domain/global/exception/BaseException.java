package com.moa.moa_server.domain.global.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {

  private final BaseErrorCode errorCode;

  public BaseException(BaseErrorCode errorCode) {
    super(errorCode.name());
    this.errorCode = errorCode;
  }

  public HttpStatus getStatus() {
    return errorCode.getStatus();
  }

  public String getCode() {
    return errorCode.name();
  }
}
