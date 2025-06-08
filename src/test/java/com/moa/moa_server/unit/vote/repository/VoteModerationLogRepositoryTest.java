package com.moa.moa_server.unit.vote.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moa.moa_server.config.jpa.JpaAuditingConfig;
import com.moa.moa_server.config.querydsl.QuerydslConfig;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteModerationLog;
import com.moa.moa_server.domain.vote.repository.VoteModerationLogRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.support.GroupTestFactory;
import com.moa.moa_server.support.UserTestFactory;
import com.moa.moa_server.support.VoteTestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({JpaAuditingConfig.class, QuerydslConfig.class})
public class VoteModerationLogRepositoryTest {

  @Autowired private VoteModerationLogRepository repository;
  @Autowired private UserRepository userRepository;
  @Autowired private GroupRepository groupRepository;

  @Autowired private VoteRepository voteRepository;

  @Test
  @DisplayName("voteId로 가장 최근 로그 1건을 조회한다")
  void findFirstByVote_IdOrderByCreatedAtDesc_성공() {
    // given
    User user = userRepository.save(UserTestFactory.createDummy());
    Group group = groupRepository.save(GroupTestFactory.createDummy(user));
    Vote vote = voteRepository.save(VoteTestFactory.createDummy(user, group));
    VoteModerationLog log1 =
        VoteModerationLog.create(
            vote,
            VoteModerationLog.ReviewResult.REJECTED,
            VoteModerationLog.ReviewReason.POLITICAL_CONTENT,
            "정치적 내용",
            "ai-v1");
    VoteModerationLog log2 =
        VoteModerationLog.create(
            vote,
            VoteModerationLog.ReviewResult.APPROVED,
            VoteModerationLog.ReviewReason.NONE,
            "",
            "ai-v2");
    repository.save(log1);
    repository.save(log2);

    // when
    VoteModerationLog found =
        repository.findFirstByVote_IdOrderByCreatedAtDesc(vote.getId()).orElseThrow();

    // then
    assertThat(found.getReviewResult()).isEqualTo(VoteModerationLog.ReviewResult.APPROVED);
    assertThat(found.getReviewReason()).isEqualTo(VoteModerationLog.ReviewReason.NONE);
  }
}
