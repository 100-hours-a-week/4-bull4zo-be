package com.moa.moa_server.domain.feedback.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class FeedbackException extends BaseException {
  public FeedbackException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
