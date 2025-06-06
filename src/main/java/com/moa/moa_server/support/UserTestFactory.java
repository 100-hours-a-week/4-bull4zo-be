package com.moa.moa_server.support;

import com.moa.moa_server.domain.user.entity.User;
import java.time.LocalDateTime;

/** 테스트용 User 엔티티 객체를 생성하는 팩토리 클래스 */
public class UserTestFactory {

  /** 테스트용 더미 User 엔티티 생성 */
  public static User createDummy() {
    return User.builder()
        .nickname("dummyUser")
        .email("dummy" + System.nanoTime() + "@test.com")
        .role(User.Role.USER)
        .userStatus(User.UserStatus.ACTIVE)
        .lastActiveAt(LocalDateTime.now())
        .build();
  }
}
