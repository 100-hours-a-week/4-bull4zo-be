package com.moa.moa_server.domain.group.util;

import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;

import java.util.regex.Pattern;

public class GroupValidator {

    private static final Pattern INVITE_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{6,8}$");

    private GroupValidator() {
        throw new AssertionError("유틸 클래스는 인스턴스화할 수 없습니다.");
    }

    public static void validateInviteCode(String inviteCode) {
        if (inviteCode == null || !INVITE_CODE_PATTERN.matcher(inviteCode).matches()) {
            throw new GroupException(GroupErrorCode.INVALID_CODE_FORMAT);
        }
    }
}
