package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.response.VoteModerationReasonResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteModerationLog;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteModerationLogRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#getModerationReason")
public class VoteModerationReasonTest {

  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock VoteModerationLogRepository voteModerationLogRepository;

  @InjectMocks VoteService voteService;

  @Nested
  class 성공_테스트 {

    @Test
    @DisplayName("투표 검열 사유 최신 로그 정상 조회")
    void getModerationReason_성공() {
      // given
      Long userId = 1L;
      Long voteId = 10L;
      User user = mock(User.class);
      Vote vote = mock(Vote.class);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(user.getId()).thenReturn(userId);
      when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
      when(vote.getUser()).thenReturn(user); // 등록자 본인 검증

      VoteModerationLog log =
          VoteModerationLog.builder()
              .reviewReason(VoteModerationLog.ReviewReason.OFFENSIVE_LANGUAGE)
              .reviewResult(VoteModerationLog.ReviewResult.REJECTED)
              .reviewDetail("비속어 포함")
              .aiVersion("v1.1.0")
              .vote(vote)
              .build();

      when(voteModerationLogRepository.findFirstByVote_IdOrderByCreatedAtDesc(voteId))
          .thenReturn(Optional.of(log));

      // when
      VoteModerationReasonResponse response = voteService.getModerationReason(userId, voteId);

      // then
      assertThat(response.voteId()).isEqualTo(voteId);
      assertThat(response.reviewReason()).isEqualTo("OFFENSIVE_LANGUAGE");
      // 필요에 따라 상세 필드 등 추가 검증
    }
  }

  @Nested
  class 실패_테스트 {

    @Test
    @DisplayName("로그가 없으면 예외 발생")
    void getModerationReason_로그없음_예외() {
      // given
      Long userId = 1L;
      Long voteId = 10L;
      User user = mock(User.class);
      Vote vote = mock(Vote.class);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(user.getId()).thenReturn(userId);
      when(voteRepository.findById(voteId)).thenReturn(Optional.of(vote));
      when(vote.getUser()).thenReturn(user);

      when(voteModerationLogRepository.findFirstByVote_IdOrderByCreatedAtDesc(voteId))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> voteService.getModerationReason(userId, voteId))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining("MODERATION_LOG_NOT_FOUND");
    }
  }
}
