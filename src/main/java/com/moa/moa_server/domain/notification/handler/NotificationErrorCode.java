package com.moa.moa_server.domain.notification.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum NotificationErrorCode implements BaseErrorCode {
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST);

  private final HttpStatus status;

  NotificationErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
