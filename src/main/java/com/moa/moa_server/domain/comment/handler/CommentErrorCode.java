package com.moa.moa_server.domain.comment.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum CommentErrorCode implements BaseErrorCode {
  INVALID_CONTENT(HttpStatus.BAD_REQUEST),
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  VOTE_NOT_FOUND(HttpStatus.NOT_FOUND),
  ;

  private final HttpStatus status;

  CommentErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
