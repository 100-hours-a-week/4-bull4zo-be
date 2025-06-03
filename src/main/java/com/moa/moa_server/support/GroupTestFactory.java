package com.moa.moa_server.support;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;

/** 테스트용 Group 엔티티 객체를 생성하는 팩토리 클래스 */
public class GroupTestFactory {

  /** 테스트용 더미 Group 엔티티 생성 */
  public static Group createDummy(User user) {
    return Group.create(user, "dummyGroup_" + System.nanoTime(), "테스트 그룹 설명", null, "INVITE12");
  }
}
