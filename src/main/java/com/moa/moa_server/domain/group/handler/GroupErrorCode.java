package com.moa.moa_server.domain.group.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum GroupErrorCode implements BaseErrorCode {
  INVALID_CODE_FORMAT(HttpStatus.BAD_REQUEST),
  INVALID_INPUT(HttpStatus.BAD_REQUEST),
  CANNOT_JOIN_PUBLIC_GROUP(HttpStatus.BAD_REQUEST),
  NOT_GROUP_OWNER(HttpStatus.FORBIDDEN),
  OWNER_CANNOT_LEAVE(HttpStatus.FORBIDDEN),
  GROUP_NOT_FOUND(HttpStatus.NOT_FOUND),
  INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND),
  MEMBERSHIP_NOT_FOUND(HttpStatus.NOT_FOUND),
  ALREADY_JOINED(HttpStatus.CONFLICT),
  DUPLICATED_NAME(HttpStatus.CONFLICT),
  INVITE_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final HttpStatus status;

  GroupErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
