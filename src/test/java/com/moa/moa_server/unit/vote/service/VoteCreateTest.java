package com.moa.moa_server.unit.vote.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.VoteService;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultRedisService;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("VoteService#createVote")
public class VoteCreateTest {

  @InjectMocks private VoteService voteService;

  @Mock private VoteRepository voteRepository;
  @Mock private UserRepository userRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private VoteResultRedisService voteResultRedisService;
  @Mock private GroupMemberRepository groupMemberRepository;

  @Mock private User user;
  @Mock private Group group;

  @BeforeEach
  void setup() {
    when(user.getUserStatus()).thenReturn(User.UserStatus.ACTIVE);

    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
  }

  @Nested
  class 성공_케이스 {
    /** Vote 저장 및 Redis 캐시 설정 후, voteId 정상 반환 확인 */
    @Test
    @DisplayName("투표 등록 성공")
    public void createVote_success() {
      // given: 요청 객체 생성
      VoteCreateRequest request =
          new VoteCreateRequest(1L, "본문", "", LocalDateTime.now().plusDays(1), false);

      // and: 그룹 공개 설정
      when(group.isPublicGroup()).thenReturn(true);

      // and: voteRepository.save()가 받은 vote 객체에 id를 수동으로 심어서 리턴
      when(voteRepository.save(any(Vote.class)))
          .thenAnswer(
              invocation -> {
                Vote savedVote = invocation.getArgument(0);
                Field idField = Vote.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(savedVote, 100L);
                return savedVote;
              });

      // when: 테스트 대상 메서드 호출
      Long result = voteService.createVote(1L, request);

      // then: 결과 및 동작 검증
      assertThat(result).isEqualTo(100L); // voteId 정상 반환
      verify(voteRepository).save(any(Vote.class)); // Vote 저장 호출 확인
      verify(voteResultRedisService)
          .setCountsWithTTL(eq(100L), anyMap(), any()); // Redis 캐시 설정 호출 확인
    }
  }

  @Nested
  class 실패_케이스 {

    /**
     * 유효하지 않은 요청으로 투표 생성 시 예외가 발생하는지 검증
     *
     * @param request 잘못된 요청 값
     * @param expectedError 기대되는 에러 코드
     */
    @ParameterizedTest
    @MethodSource("invalidVoteRequests")
    @DisplayName("잘못된 요청 값")
    public void createVote_fail_InvalidInput(
        VoteCreateRequest request, VoteErrorCode expectedError) {
      // given
      when(group.isPublicGroup()).thenReturn(true);

      // when & then: 예외 발생 검증
      assertThatThrownBy(() -> voteService.createVote(1L, request))
          .isInstanceOf(VoteException.class) // Vote 예외 타입인지 확인
          .hasMessageContaining(expectedError.name()); // 예외 메시지에 에러 코드 포함 확인
    }

    /** 그룹에 소속되지 않은 사용자가 투표 생성 시 403 예외 발생 */
    @Test
    @DisplayName("그룹 미소속 사용자")
    public void createVote_fail_NotGroupMember() {
      // given
      VoteCreateRequest request =
          new VoteCreateRequest(1L, "본문", "", LocalDateTime.now().plusDays(1), false);

      // 그룹이 공개 그룹이 아니고, 유저는 멤버가 아님
      when(group.isPublicGroup()).thenReturn(false);
      when(groupMemberRepository.findByGroupAndUser(group, user)).thenReturn(Optional.empty());

      // when & then: 예외 발생 검증
      assertThatThrownBy(() -> voteService.createVote(1L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.NOT_GROUP_MEMBER.name());
    }

    /** 존재하지 않는 그룹 ID로 투표 생성 시 404 예외 발생 */
    @Test
    @DisplayName("존재하지 않는 그룹")
    public void createVote_fail_GroupNotFound() {
      // given: 요청은 정상이나, 그룹 ID에 해당하는 그룹이 없음
      VoteCreateRequest request =
          new VoteCreateRequest(1L, "본문", "", LocalDateTime.now().plusDays(1), false);

      // 그룹 조회 실패 (Optional.empty())
      when(groupRepository.findById(1L)).thenReturn(Optional.empty());

      // when & then: GROUP_NOT_FOUND 예외 검증
      assertThatThrownBy(() -> voteService.createVote(1L, request))
          .isInstanceOf(VoteException.class)
          .hasMessageContaining(VoteErrorCode.GROUP_NOT_FOUND.name());
    }

    static Stream<Arguments> invalidVoteRequests() {
      return Stream.of(
          Arguments.of( // 본문 길이 초과
              new VoteCreateRequest(
                  1L, "A".repeat(101), "", LocalDateTime.now().plusDays(1), false),
              VoteErrorCode.INVALID_CONTENT),
          Arguments.of( // 이미지 URL 잘못됨
              new VoteCreateRequest(
                  1L, "정상 본문", "not-a-url", LocalDateTime.now().plusDays(1), false),
              VoteErrorCode.INVALID_URL),
          Arguments.of( // 종료 시각 과거
              new VoteCreateRequest(1L, "정상 본문", "", LocalDateTime.now().minusDays(1), false),
              VoteErrorCode.INVALID_TIME));
    }
  }
}
