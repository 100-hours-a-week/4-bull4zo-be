package com.moa.moa_server.domain.user.util;

import com.moa.moa_server.domain.auth.handler.AuthErrorCode;
import com.moa.moa_server.domain.auth.handler.AuthException;
import com.moa.moa_server.domain.user.entity.User;

public class AuthUserValidator {
    private AuthUserValidator() {
        throw new AssertionError("유틸 클래스는 인스턴스화할 수 없습니다.");
    }

    public static void validateActive(User user) {
        if (user == null) {
            throw new AuthException(AuthErrorCode.USER_NOT_FOUND);
        }
        if (user.getUserStatus() == User.UserStatus.WITHDRAWN ||
                user.getUserStatus() == User.UserStatus.DORMANT) {
            throw new AuthException(AuthErrorCode.USER_WITHDRAWN);
        }
    }
}
