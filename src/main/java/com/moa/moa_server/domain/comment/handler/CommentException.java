package com.moa.moa_server.domain.comment.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class CommentException extends BaseException {
  public CommentException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
