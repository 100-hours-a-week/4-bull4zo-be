package com.moa.moa_server.domain.group.util;

import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import java.util.regex.Pattern;

public class GroupValidator {

  private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6,8}$");
  private static final Pattern NAME_PATTERN =
      Pattern.compile("^(?![\\d\\s])[가-힣a-zA-Z0-9 ]{2,12}$");
  private static final String UPLOAD_URL_PREFIX = "https://upload-domain/group";

  private GroupValidator() {
    throw new AssertionError("유틸 클래스는 인스턴스화할 수 없습니다.");
  }

  public static void validateInviteCode(String inviteCode) {
    if (inviteCode == null || !INVITE_CODE_PATTERN.matcher(inviteCode).matches()) {
      throw new GroupException(GroupErrorCode.INVALID_CODE_FORMAT);
    }
  }

  public static void validateGroupName(String name) {
    if (name == null || name.isBlank() || !NAME_PATTERN.matcher(name).matches()) {
      throw new GroupException(GroupErrorCode.INVALID_INPUT);
    }
  }

  public static void validateDescription(String description) {
    if (description == null
        || description.isBlank()
        || description.length() < 2
        || description.length() > 50) {
      throw new GroupException(GroupErrorCode.INVALID_INPUT);
    }
  }

  public static void validateImageUrl(String imageUrl) {
    if (imageUrl != null && !imageUrl.isBlank() && !imageUrl.startsWith(UPLOAD_URL_PREFIX)) {
      throw new GroupException(GroupErrorCode.INVALID_INPUT);
    }
  }
}
