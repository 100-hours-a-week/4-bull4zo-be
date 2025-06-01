package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#getVoteDetail")
public class VoteDetailTest {

  @InjectMocks private VoteService voteService;

  @Mock private VoteRepository voteRepository;
  @Mock private VoteResponseRepository voteResponseRepository;
  @Mock private UserRepository userRepository;

  @Mock private User user;
  @Mock private Vote vote;
  @Mock private Group group;

  @Nested
  class 성공_케이스 {

    @BeforeEach
    void setup() {
      when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.OPEN);
      when(vote.getGroup()).thenReturn(group);
      when(vote.getId()).thenReturn(10L);
    }

    @Test
    @DisplayName("등록자가 조회")
    void getVoteDetail_success_author() {
      // given
      when(vote.getUser()).thenReturn(user);
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));

      // when
      VoteDetailResponse result = voteService.getVoteDetail(1L, 10L);

      // then
      assertThat(result).isNotNull();
      assertThat(result.voteId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("참여자(유효 응답)가 조회")
    void getVoteDetail_success_participant() {
      // given: 투표에 응답한 참여자가 조회 (유효 응답: 1 또는 2)
      User user2 = mock(User.class);
      when(user2.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
      when(vote.getUser()).thenReturn(mock(User.class)); // 작성자와 다름
      VoteResponse voteResponse = mock(VoteResponse.class);
      when(voteResponse.getOptionNumber()).thenReturn(1);
      when(voteResponseRepository.findByVoteAndUser(vote, user2))
          .thenReturn(Optional.of(voteResponse));

      // when
      VoteDetailResponse result = voteService.getVoteDetail(2L, 10L);

      // then
      assertThat(result).isNotNull();
    }
  }

  @Nested
  class 실패_케이스 {
    // 투표 상태: PENDING/REJECTED 상태는 조회 불가. 투표가 정상 등록된 상태일 때만 조회 가능함 -> 403
    // 조회 권한: 참여자이지만 기권으로 투표한 경우, 투표하지 않은 경우 -> 403
    // 투표 없음 -> 404

    @Test
    @DisplayName("투표 상태 비정상(PENDING, REJECTED)")
    void getVoteDetail_fail_notOpenOrClosed() {
      // given
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.PENDING);
      when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));

      // when & then
      assertThatThrownBy(() -> voteService.getVoteDetail(1L, 10L))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.FORBIDDEN.name());
    }

    @Test
    @DisplayName("참여자/등록자 아님")
    void getVoteDetail_fail_notAuthorNorParticipant() {
      // given
      User stranger = mock(User.class);
      when(stranger.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(3L)).thenReturn(Optional.of(stranger));
      when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.OPEN);
      when(vote.getUser()).thenReturn(mock(User.class)); // 작성자와 다름
      when(voteResponseRepository.findByVoteAndUser(vote, stranger))
          .thenReturn(Optional.empty()); // 참여 응답 없음

      // when & then
      assertThatThrownBy(() -> voteService.getVoteDetail(3L, 10L))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.FORBIDDEN.name());
    }

    @Test
    @DisplayName("참여했지만 기권(0) 응답")
    void getVoteDetail_fail_participatedButAbstained() {
      // given
      User stranger = mock(User.class);
      when(stranger.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(4L)).thenReturn(Optional.of(stranger));
      when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.OPEN);
      when(vote.getUser()).thenReturn(mock(User.class)); // 작성자와 다름

      // and: 기권(0)으로 응답
      VoteResponse abstained = mock(VoteResponse.class);
      when(abstained.getOptionNumber()).thenReturn(0);
      when(voteResponseRepository.findByVoteAndUser(vote, stranger))
          .thenReturn(Optional.of(abstained));

      // when & then
      assertThatThrownBy(() -> voteService.getVoteDetail(4L, 10L))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.FORBIDDEN.name());
    }

    @Test
    @DisplayName("존재하지 않는 투표 ID")
    void getVoteDetail_fail_voteNotFound() {
      // given
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(voteRepository.findById(999L)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> voteService.getVoteDetail(1L, 999L))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.VOTE_NOT_FOUND.name());
    }
  }
}
