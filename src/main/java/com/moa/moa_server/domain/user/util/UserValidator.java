package com.moa.moa_server.domain.user.util;

import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import java.util.regex.Pattern;

public class UserValidator {

  private static final Pattern NICKNAME_PATTERN =
      Pattern.compile("^(?![\\d\\s])[가-힣a-zA-Z0-9]{2,10}$");

  private UserValidator() {
    throw new AssertionError("유틸 클래스는 인스턴스화할 수 없습니다.");
  }

  public static void validateNickname(String nickname) {
    if (nickname == null || nickname.isBlank() || !NICKNAME_PATTERN.matcher(nickname).matches()) {
      throw new UserException(UserErrorCode.INVALID_NICKNAME);
    }
  }
}
