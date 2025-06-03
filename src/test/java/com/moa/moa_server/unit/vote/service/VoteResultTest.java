package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import java.util.List;
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
@DisplayName("VoteService#getVoteResult")
public class VoteResultTest {

  @InjectMocks private VoteService voteService;

  @Mock private VoteRepository voteRepository;
  @Mock private VoteResponseRepository voteResponseRepository;
  @Mock private UserRepository userRepository;
  @Mock private VoteResultService voteResultService;

  @Mock private User user;
  @Mock private Vote vote;

  @Nested
  class 성공_케이스 {

    @BeforeEach
    void setup() {
      when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));
      when(vote.getVoteStatus()).thenReturn(Vote.VoteStatus.OPEN);
    }

    @Test
    @DisplayName("등록자가 조회")
    void getVoteResult_success_author() {
      // given
      when(userRepository.findById(1L)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(vote.getUser()).thenReturn(user);
      when(voteResponseRepository.findByVoteAndUser(vote, user))
          .thenReturn(Optional.empty()); // 응답 X
      when(voteResponseRepository.findAllByVote(vote)).thenReturn(List.of());
      when(voteResultService.getResults(vote)).thenReturn(List.of());

      // when
      var result = voteService.getVoteResult(1L, 10L);

      // then
      assertThat(result).isNotNull();
      assertThat(result.voteId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("참여자(유효 응답)가 조회")
    void getVoteResult_success_participant() {
      // given
      User participant = mock(User.class);
      when(participant.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(userRepository.findById(2L)).thenReturn(Optional.of(participant));
      when(vote.getUser()).thenReturn(mock(User.class)); // 등록자 아님
      VoteResponse voteResponse = mock(VoteResponse.class);
      when(voteResponse.getOptionNumber()).thenReturn(1);
      when(voteResponseRepository.findByVoteAndUser(vote, participant))
          .thenReturn(Optional.of(voteResponse));
      when(voteResponseRepository.findAllByVote(vote)).thenReturn(List.of(voteResponse));
      when(voteResultService.getResults(vote)).thenReturn(List.of());

      // when
      var result = voteService.getVoteResult(2L, 10L);

      // then
      assertThat(result).isNotNull();
      assertThat(result.userResponse()).isEqualTo(1);
    }
  }

  @Nested
  class 실패_케이스 {
    // 투표 상태: PENDING/REJECTED 상태는 조회 불가. 투표가 정상 등록된 상태일 때만 조회 가능함 -> 403
    // 조회 권한: 참여자이지만 기권으로 투표한 경우, 투표하지 않은 경우 -> 403
    // 투표 없음 -> 404

    // getVoteDetail과 동일한 예외 처리 케이스로, 중복 테스트 생략
  }
}
