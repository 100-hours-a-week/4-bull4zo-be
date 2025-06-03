package com.moa.moa_server.domain.image.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum ImageErrorCode implements BaseErrorCode {
  INVALID_FILE(HttpStatus.BAD_REQUEST),
  INVALID_URL(HttpStatus.BAD_REQUEST),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND),
  AWS_S3_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus status;

  ImageErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
