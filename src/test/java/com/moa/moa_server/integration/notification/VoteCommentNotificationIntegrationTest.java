package com.moa.moa_server.integration.notification;

import static com.moa.moa_server.util.TestFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.service.CommentService;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.notification.repository.NotificationRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** 댓글 생성 알림 테스트 */
@SpringBootTest
@ActiveProfiles("test")
public class VoteCommentNotificationIntegrationTest {

  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired CommentService commentService;
  @Autowired NotificationRepository notificationRepository;
  @Autowired private VoteResponseRepository voteResponseRepository;

  User author;
  User commenter;
  Group group;
  Vote vote;

  @BeforeEach
  void setUp() {
    notificationRepository.deleteAll();
    voteResponseRepository.deleteAll();
    String suffix = UUID.randomUUID().toString().substring(0, 4);
    author = userRepository.save(user("투표등록자" + suffix));
    commenter = userRepository.save(user("댓글작성자" + suffix));
    group = groupRepository.save(group(author, "group" + suffix));
    vote = voteRepository.save(vote(author, group, Vote.VoteStatus.OPEN)); // 댓글 달 투표
  }

  @Test
  @DisplayName("댓글 작성 시 투표 작성자에게 알림이 저장된다")
  void testNotificationIsSavedAfterComment() throws InterruptedException {
    // given
    voteResponseRepository.save(voteResponse(vote, commenter, 1)); // 투표 참여 후 댓글 작성 가능
    CommentCreateRequest request = new CommentCreateRequest("댓글 달기", false);

    // when
    commentService.createComment(commenter.getId(), vote.getId(), request);

    // then (비동기 실행 시간 확보)
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var notifications = notificationRepository.findAll();
              assertThat(notifications).hasSize(1);
              assertThat(notifications.getFirst().getUser().getId()).isEqualTo(author.getId());
              assertThat(notifications.getFirst().getType().name()).isEqualTo("MY_VOTE_COMMENT");
              assertThat(notifications.getFirst().getContent()).contains("댓글");
            });
  }

  @Test
  @DisplayName("본인이 작성한 투표에 댓글 작성 시 알림이 생성되지 않는다")
  void noNotificationWhenAuthorCommentsOwnVote() {
    // given
    voteResponseRepository.save(voteResponse(vote, author, 1)); // 본인도 참여했다고 가정
    var request = new CommentCreateRequest("내 투표에 내가 댓글", false);

    // when
    commentService.createComment(author.getId(), vote.getId(), request);

    // then
    await()
        .atMost(1, TimeUnit.SECONDS)
        .untilAsserted(() -> assertThat(notificationRepository.findAll()).isEmpty());
  }

  @Test
  @DisplayName("댓글 내용이 36자를 초과하면 알림 내용이 36자로 잘린 후 '...'이 붙는다")
  void notificationContentIsTrimmedIfCommentIsTooLong() {
    // given
    voteResponseRepository.save(voteResponse(vote, commenter, 1));
    String longComment = "이 댓글은 36자를 넘는 매우 긴 댓글입니다. 총 37글자 입니다.";
    CommentCreateRequest request = new CommentCreateRequest(longComment, false);

    // when
    commentService.createComment(commenter.getId(), vote.getId(), request);

    // then
    await()
        .atMost(2, TimeUnit.SECONDS)
        .untilAsserted(
            () -> {
              var notifications = notificationRepository.findAll();
              assertThat(notifications).hasSize(1);
              var content = notifications.getFirst().getContent();
              assertThat(content.length()).isLessThanOrEqualTo(39); // 36 + "..."
              assertThat(content).startsWith(longComment.substring(0, 36));
              assertThat(content).endsWith("...");
            });
  }
}
