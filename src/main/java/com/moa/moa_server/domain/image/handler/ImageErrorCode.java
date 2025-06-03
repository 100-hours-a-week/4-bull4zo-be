package com.moa.moa_server.domain.image.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ImageErrorCode implements BaseErrorCode {
  INVALID_FILE(HttpStatus.BAD_REQUEST);

  private final HttpStatus status;

  ImageErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
