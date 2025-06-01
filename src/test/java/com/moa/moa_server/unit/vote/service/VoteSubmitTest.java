package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultRedisService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#submitVote")
public class VoteSubmitTest {

  @InjectMocks private VoteService voteService;

  @Mock private VoteRepository voteRepository;
  @Mock private VoteResponseRepository voteResponseRepository;
  @Mock private UserRepository userRepository;
  @Mock private VoteResultRedisService voteResultRedisService;
  @Mock private GroupMemberRepository groupMemberRepository;

  @Mock private User user;
  @Mock private Vote vote;
  @Mock private Group group;

  @BeforeEach
  void setup() {
    when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(voteRepository.findById(10L)).thenReturn(Optional.of(vote));
    when(vote.isOpen()).thenReturn(true);
    when(vote.getGroup()).thenReturn(group);
    when(group.isPublicGroup()).thenReturn(true);
  }

  @Nested
  class 성공_케이스 {
    @Test
    @DisplayName("정상 투표 제출(공개 그룹)")
    void submitVote_success_public() {
      // given
      VoteSubmitRequest request = new VoteSubmitRequest(1);
      when(voteResponseRepository.existsByVoteAndUser(vote, user)).thenReturn(false);
      VoteResponse vr = mock(VoteResponse.class);
      when(voteResponseRepository.save(any(VoteResponse.class))).thenReturn(vr);

      // when
      voteService.submitVote(1L, 10L, request);

      // then
      verify(voteResponseRepository).save(any(VoteResponse.class));
      verify(voteResultRedisService).incrementOptionCount(10L, 1);
    }

    @Test
    @DisplayName("비공개 그룹, 그룹 멤버만 투표 가능")
    void submitVote_success_private_member() {
      // given
      when(group.isPublicGroup()).thenReturn(false);
      when(groupMemberRepository.findByGroupAndUser(group, user))
          .thenReturn(Optional.of(mock(GroupMember.class)));
      VoteSubmitRequest request = new VoteSubmitRequest(2);
      when(voteResponseRepository.existsByVoteAndUser(vote, user)).thenReturn(false);

      // when
      voteService.submitVote(1L, 10L, request);

      // then
      verify(voteResponseRepository).save(any(VoteResponse.class));
      verify(voteResultRedisService).incrementOptionCount(10L, 2);
    }
  }

  @Nested
  class 실패_케이스 {
    @Test
    @DisplayName("이미 투표한 유저")
    void submitVote_fail_alreadyVoted() {
      // given
      VoteSubmitRequest request = new VoteSubmitRequest(1);
      when(voteResponseRepository.existsByVoteAndUser(vote, user)).thenReturn(true);

      // when & then
      assertThatThrownBy(() -> voteService.submitVote(1L, 10L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.ALREADY_VOTED.name());
    }

    @Test
    @DisplayName("잘못된 응답값")
    void submitVote_fail_invalidOption() {
      // given
      VoteSubmitRequest request = new VoteSubmitRequest(9); // 0,1,2만 유효

      // when & then
      assertThatThrownBy(() -> voteService.submitVote(1L, 10L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.INVALID_OPTION.name());
    }

    @Test
    @DisplayName("비공개 그룹, 미가입자 예외")
    void submitVote_fail_private_not_member() {
      // given
      when(group.isPublicGroup()).thenReturn(false);
      when(groupMemberRepository.findByGroupAndUser(group, user)).thenReturn(Optional.empty());

      VoteSubmitRequest request = new VoteSubmitRequest(1);

      // when & then
      assertThatThrownBy(() -> voteService.submitVote(1L, 10L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.NOT_GROUP_MEMBER.name());
    }

    @Test
    @DisplayName("투표가 이미 닫힘")
    void submitVote_fail_voteClosed() {
      // given
      when(vote.isOpen()).thenReturn(false);
      VoteSubmitRequest request = new VoteSubmitRequest(1);

      // when & then
      assertThatThrownBy(() -> voteService.submitVote(1L, 10L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.VOTE_NOT_OPENED.name());
    }

    @Test
    @DisplayName("존재하지 않는 투표 ID")
    void submitVote_fail_voteNotFound() {
      // given
      when(voteRepository.findById(999L)).thenReturn(Optional.empty());
      VoteSubmitRequest request = new VoteSubmitRequest(1);

      // when & then
      assertThatThrownBy(() -> voteService.submitVote(1L, 999L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining("VOTE_NOT_FOUND");
    }
  }
}
