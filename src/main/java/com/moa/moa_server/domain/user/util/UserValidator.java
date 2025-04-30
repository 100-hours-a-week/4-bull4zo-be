package com.moa.moa_server.domain.user.util;

import com.moa.moa_server.domain.user.entity.User;

public class UserValidator {
    private UserValidator() {
        throw new AssertionError("유틸 클래스는 인스턴스화할 수 없습니다.");
    }

    public static void validateActive(User user) {
        if (user == null ||
                user.getUserStatus() == User.UserStatus.WITHDRAWN ||
                user.getUserStatus() == User.UserStatus.DORMANT) {
            throw new IllegalStateException("USER_NOT_FOUND");
        }
    }
}
