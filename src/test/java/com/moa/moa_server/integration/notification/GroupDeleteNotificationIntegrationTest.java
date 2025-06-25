package com.moa.moa_server.integration.notification;

import static com.moa.moa_server.util.TestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.moa.moa_server.domain.group.dto.group_manage.GroupDeleteResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.service.GroupService;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** 그룹 삭제 알림 테스트 */
@SpringBootTest
@ActiveProfiles("test")
public class GroupDeleteNotificationIntegrationTest {

  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired GroupMemberRepository groupMemberRepository;
  @Autowired GroupService groupService;
  @Autowired NotificationRepository notificationRepository;

  User owner;
  User member1;
  User member2;
  Group group;

  @BeforeEach
  void setUp() {
    String suffix = UUID.randomUUID().toString().substring(0, 4);
    owner = userRepository.save(user("소유자" + suffix));
    member1 = userRepository.save(user("멤버1" + suffix));
    member2 = userRepository.save(user("멤버2" + suffix));
    group = groupRepository.save(group(owner, "그룹" + suffix));

    groupMemberRepository.save(groupMember(owner, group, "OWNER"));
    groupMemberRepository.save(groupMember(member1, group, "MEMBER"));
    groupMemberRepository.save(groupMember(member2, group, "MEMBER"));
  }

  @Test
  @DisplayName("그룹이 삭제되면 모든 활성 멤버에게 알림이 전송된다")
  void notifyWhenGroupDeleted() {
    // when
    GroupDeleteResponse response = groupService.deleteGroup(owner.getId(), group.getId());

    // then
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var expectedContent = group.getName() + " 그룹이 삭제되었습니다.";
              var notifications =
                  notificationRepository.findAll().stream()
                      .filter(n -> expectedContent.equals(n.getContent()))
                      .toList();
              assertThat(notifications).hasSize(2); // owner 제외한 2명에게 발송
              List<Long> notifiedUserIds =
                  notifications.stream().map(n -> n.getUser().getId()).toList();
              assertThat(notifiedUserIds)
                  .containsExactlyInAnyOrder(member1.getId(), member2.getId());
            });
  }

  @Test
  @DisplayName("그룹을 탈퇴한 멤버에게는 알림이 생성되지 않는다")
  void noNotificationForWithdrawnMembers() {
    // given: member2가 soft delete 처리됨 (탈퇴)
    var withdrawnMember = groupMemberRepository.findByGroupAndUser(group, member2).orElseThrow();
    withdrawnMember.leave(); // 그룹 탈퇴
    groupMemberRepository.saveAndFlush(withdrawnMember);

    // when
    groupService.deleteGroup(owner.getId(), group.getId());

    // then
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var expectedContent = group.getName() + " 그룹이 삭제되었습니다.";
              var notifications =
                  notificationRepository.findAll().stream()
                      .filter(n -> expectedContent.equals(n.getContent()))
                      .toList();
              assertThat(notifications).hasSize(1); // member1만 알림 수신
              var notifiedUserIds = notifications.stream().map(n -> n.getUser().getId()).toList();
              assertThat(notifiedUserIds).containsExactly(member1.getId());
            });
  }
}
