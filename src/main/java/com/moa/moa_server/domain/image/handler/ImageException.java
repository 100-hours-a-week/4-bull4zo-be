package com.moa.moa_server.domain.image.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class ImageException extends BaseException {
  public ImageException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
