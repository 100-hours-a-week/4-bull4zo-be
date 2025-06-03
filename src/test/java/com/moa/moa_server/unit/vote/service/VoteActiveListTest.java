package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.response.active.ActiveVoteResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#getActiveVoteList")
public class VoteActiveListTest {

  @InjectMocks private VoteService voteService;

  @Mock private VoteRepository voteRepository;
  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private GroupMemberRepository groupMemberRepository;

  @Mock private User user;
  @Mock private Vote vote;
  @Mock private Group group;

  @Nested
  class 비로그인_성공_케이스 {
    @Test
    @DisplayName("공개 그룹 투표 리스트 정상 조회")
    void getActiveVotes_unauthenticated_success() {
      // given
      when(group.isPublicGroup()).thenReturn(true);
      when(group.getId()).thenReturn(1L);
      when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
      when(voteRepository.findActiveVotes(
              eq(List.of(group)),
              any(), // cursor
              isNull(), // user
              anyInt()))
          .thenReturn(List.of(vote));
      when(vote.getId()).thenReturn(10L);
      when(vote.getGroup()).thenReturn(group);
      when(vote.getUser()).thenReturn(user);
      when(vote.getVoteType()).thenReturn(Vote.VoteType.USER);
      when(user.getNickname()).thenReturn("test");

      // when
      ActiveVoteResponse result = voteService.getActiveVotes(null, 1L, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.votes()).hasSize(1); // 투표 1개만 반환됨을 검증
      assertThat(result.votes().getFirst().voteId()).isEqualTo(10L); // 반환된 투표의 ID가 10인지 확인
      assertThat(result.votes().getFirst().groupId()).isEqualTo(1L); // groupId가 1인지 확인
    }
  }

  @Nested
  class 비로그인_실패_케이스 {
    @Test
    @DisplayName("비공개 그룹 접근 시 403")
    void getActiveVotes_unauthenticated_privateGroup_forbidden() {
      // given
      Group privateGroup = mock(Group.class);
      when(privateGroup.isPublicGroup()).thenReturn(false);
      when(groupRepository.findById(1L)).thenReturn(Optional.of(privateGroup));

      // when & then
      assertThatThrownBy(() -> voteService.getActiveVotes(null, 1L, null, null))
          .isInstanceOf(com.moa.moa_server.domain.vote.handler.VoteException.class)
          .hasMessageContaining(VoteErrorCode.FORBIDDEN.name());
    }
  }

  @Nested
  class 로그인_성공_케이스 {
    @Test
    @DisplayName("로그인 사용자, 공개 그룹 투표 리스트 정상 조회")
    void getActiveVotes_authenticated_success() {
      // given
      Long userId = 42L;
      Long groupId = 1L;

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);

      when(group.isPublicGroup()).thenReturn(true);
      when(group.getId()).thenReturn(groupId);
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      // 사용자의 접근 가능한 그룹이 공개 그룹만 있다고 가정
      when(voteRepository.findActiveVotes(eq(List.of(group)), any(), eq(user), anyInt()))
          .thenReturn(List.of(vote));

      // 반환되는 투표 객체
      when(vote.getId()).thenReturn(100L);
      when(vote.getGroup()).thenReturn(group);
      when(vote.getUser()).thenReturn(user);
      when(vote.getVoteType()).thenReturn(Vote.VoteType.USER);
      when(user.getNickname()).thenReturn("testUser");
      when(vote.getClosedAt()).thenReturn(LocalDateTime.now().plusDays(1));
      when(vote.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(1));

      // when
      ActiveVoteResponse result = voteService.getActiveVotes(userId, groupId, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.votes()).hasSize(1);
      assertThat(result.votes().getFirst().voteId()).isEqualTo(100L);
      assertThat(result.votes().getFirst().groupId()).isEqualTo(groupId);
      assertThat(result.votes().getFirst().authorNickname()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("로그인 사용자, 비공개 그룹 투표 리스트 정상 조회")
    void getActiveVotes_authenticated_privateGroup_success() {
      // given
      Long userId = 42L;
      Long groupId = 2L;

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);

      // 비공개 그룹 세팅
      when(group.isPublicGroup()).thenReturn(false);
      when(group.getId()).thenReturn(groupId);
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      // 그룹 멤버 세팅
      when(groupMemberRepository.findByGroupAndUser(group, user))
          .thenReturn(Optional.of(mock(com.moa.moa_server.domain.group.entity.GroupMember.class)));

      // 투표 데이터 (해당 그룹의 투표 1개)
      when(voteRepository.findActiveVotes(eq(List.of(group)), any(), eq(user), anyInt()))
          .thenReturn(List.of(vote));

      when(vote.getId()).thenReturn(101L);
      when(vote.getGroup()).thenReturn(group);
      when(vote.getUser()).thenReturn(user);
      when(vote.getVoteType()).thenReturn(Vote.VoteType.USER);
      when(user.getNickname()).thenReturn("privateUser");
      when(vote.getClosedAt()).thenReturn(LocalDateTime.now().plusDays(1));
      when(vote.getCreatedAt()).thenReturn(LocalDateTime.now().minusDays(1));

      // when
      ActiveVoteResponse result = voteService.getActiveVotes(userId, groupId, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.votes()).hasSize(1);
      assertThat(result.votes().getFirst().voteId()).isEqualTo(101L);
      assertThat(result.votes().getFirst().groupId()).isEqualTo(groupId);
      assertThat(result.votes().getFirst().authorNickname()).isEqualTo("privateUser");
    }
  }

  @Nested
  class 로그인_실패_케이스 {
    @Test
    @DisplayName("비공개 그룹인데 멤버가 아닌 경우 FORBIDDEN 예외")
    void getActiveVotes_authenticated_privateGroup_notMember_forbidden() {
      // given
      Long userId = 42L;
      Long groupId = 2L;

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);

      when(group.isPublicGroup()).thenReturn(false);
      when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

      // when & then
      assertThatThrownBy(() -> voteService.getActiveVotes(userId, groupId, null, null))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.FORBIDDEN.name());
    }

    @Test
    @DisplayName("존재하지 않는 그룹 id로 요청 시 GROUP_NOT_FOUND 예외")
    void getActiveVotes_authenticated_groupNotFound() {
      // given
      Long userId = 42L;
      Long groupId = 999L;
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);
      when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> voteService.getActiveVotes(userId, groupId, null, null))
          .isInstanceOf(com.moa.moa_server.domain.vote.handler.VoteException.class)
          .hasMessageContaining(VoteErrorCode.GROUP_NOT_FOUND.name());
    }
  }
}
