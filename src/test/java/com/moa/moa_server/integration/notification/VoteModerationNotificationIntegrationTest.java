package com.moa.moa_server.integration.notification;

import static com.moa.moa_server.util.TestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.notification.entity.NotificationType;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.VoteModerationService;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** 투표 검열 결과 변화(승인/거절) 알림 테스트 */
@SpringBootTest
@ActiveProfiles("test")
public class VoteModerationNotificationIntegrationTest {

  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired NotificationRepository notificationRepository;
  @Autowired private VoteModerationService voteModerationService;

  User author;
  Group group;
  Vote vote;

  @BeforeEach
  void setUp() {
    notificationRepository.deleteAll();
    String suffix = UUID.randomUUID().toString().substring(0, 4);
    author = userRepository.save(user("투표등록자" + suffix));
    group = groupRepository.save(group(author, "group" + suffix));
    vote = voteRepository.save(vote(author, group, Vote.VoteStatus.PENDING));
  }

  @Test
  @DisplayName("투표가 승인되면 작성자에게 VOTE_APPROVED 알림이 저장된다")
  void notifyWhenVoteApproved() {
    // given
    VoteModerationCallbackRequest request =
        new VoteModerationCallbackRequest(vote.getId(), "APPROVED", "NONE", "정상", "v1");

    // when
    voteModerationService.handleCallback(request);

    // then
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var notifications = notificationRepository.findAll();
              assertThat(notifications).hasSize(1);
              assertThat(notifications.getFirst().getUser().getId()).isEqualTo(author.getId());
              assertThat(notifications.getFirst().getType())
                  .isEqualTo(NotificationType.VOTE_APPROVED);
              assertThat(notifications.getFirst().getContent())
                  .contains(
                      vote.getContent().substring(0, Math.min(10, vote.getContent().length())));
            });
  }

  @Test
  @DisplayName("투표가 거절되면 작성자에게 VOTE_REJECTED 알림이 저장된다")
  void notifyWhenVoteRejected() {
    // given
    VoteModerationCallbackRequest request =
        new VoteModerationCallbackRequest(
            vote.getId(), "REJECTED", "OFFENSIVE_LANGUAGE", "부적절", "v1");

    // when
    voteModerationService.handleCallback(request);

    // then
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var notifications = notificationRepository.findAll();
              assertThat(notifications).hasSize(1);
              var notification = notifications.getFirst();
              assertThat(notification.getUser().getId()).isEqualTo(author.getId());
              assertThat(notification.getType()).isEqualTo(NotificationType.VOTE_REJECTED);
              assertThat(notification.getContent())
                  .contains(
                      vote.getContent().substring(0, Math.min(10, vote.getContent().length())));
              assertThat(notification.getRedirectUrl()).isNull();
            });
  }
}
