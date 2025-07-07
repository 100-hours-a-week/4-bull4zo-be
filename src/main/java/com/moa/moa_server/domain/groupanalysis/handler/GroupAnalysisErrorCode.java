package com.moa.moa_server.domain.groupanalysis.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import org.springframework.http.HttpStatus;

public enum GroupAnalysisErrorCode implements BaseErrorCode {
  INVALID_TIME(HttpStatus.BAD_REQUEST),
  DUPLICATE_ANALYSIS(HttpStatus.CONFLICT),
  MONGO_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR),
  ;

  private final HttpStatus status;

  GroupAnalysisErrorCode(HttpStatus status) {
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
