package com.moa.moa_server.domain.vote.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum VoteErrorCode implements BaseErrorCode {
  INVALID_CONTENT(HttpStatus.BAD_REQUEST),
  INVALID_URL(HttpStatus.BAD_REQUEST),
  INVALID_TIME(HttpStatus.BAD_REQUEST),
  GROUP_NOT_FOUND(HttpStatus.NOT_FOUND),
  NOT_GROUP_MEMBER(HttpStatus.FORBIDDEN),
  VOTE_NOT_FOUND(HttpStatus.NOT_FOUND),
  FORBIDDEN(HttpStatus.FORBIDDEN),
  INVALID_OPTION(HttpStatus.BAD_REQUEST),
  ALREADY_VOTED(HttpStatus.CONFLICT),
  VOTE_NOT_OPENED(HttpStatus.FORBIDDEN),
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST),
  INVALID_MODERATION_RESULT(HttpStatus.BAD_REQUEST),
  ;

  private final HttpStatus status;

  VoteErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
