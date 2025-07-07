package com.moa.moa_server.domain.groupanalysis.handler;

import com.moa.moa_server.domain.global.exception.BaseErrorCode;
import com.moa.moa_server.domain.global.exception.BaseException;

public class GroupAnalysisException extends BaseException {
  public GroupAnalysisException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
