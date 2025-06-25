package com.moa.moa_server.domain.notification.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class NotificationException extends BaseException {
  public NotificationException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
