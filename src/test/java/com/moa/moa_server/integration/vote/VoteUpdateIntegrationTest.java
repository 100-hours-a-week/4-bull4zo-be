package com.moa.moa_server.integration.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.moa_server.domain.auth.service.JwtTokenService;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class VoteUpdateIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired ObjectMapper objectMapper;
  @Autowired JwtTokenService jwtTokenService;

  User testUser;
  Group testGroup;
  Vote testVote;
  String accessToken;

  @BeforeEach
  void setup() {
    // 1. User 저장
    testUser =
        userRepository.save(
            User.builder()
                .nickname("testuser")
                .email("test@moa.com")
                .role(User.Role.USER)
                .userStatus(User.UserStatus.ACTIVE)
                .lastActiveAt(LocalDateTime.now())
                .build());

    // 2. Group 저장
    testGroup =
        groupRepository.save(
            Group.builder()
                .name("공개")
                .inviteCode("000000")
                .user(testUser)
                .description("테스트 그룹입니다.")
                .build());

    // 3. Vote 저장
    testVote =
        voteRepository.save(
            Vote.createUserVote(
                testUser,
                testGroup,
                "본문입니다",
                "",
                LocalDateTime.now().plusDays(1),
                false,
                Vote.VoteStatus.REJECTED,
                false));

    // 4. JWT 토큰
    accessToken = jwtTokenService.issueAccessToken(testUser.getId());
  }

  @Nested
  class 성공_케이스 {

    @Test
    @DisplayName("투표 수정 통합 - 본인 REJECTED 투표 정상 수정")
    void updateVote_success() throws Exception {
      // given
      VoteUpdateRequest request =
          new VoteUpdateRequest("수정된 본문", "", LocalDateTime.now().plusDays(3));

      // when
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("SUCCESS"))
          .andExpect(jsonPath("$.data.voteId").value(testVote.getId()));

      // then: 실제 DB에서 값 확인
      Vote updated = voteRepository.findById(testVote.getId()).orElseThrow();
      assertThat(updated.getContent()).isEqualTo("수정된 본문");
      assertThat(updated.getImageUrl()).isEmpty(); // 빈 문자열로 삭제 처리
      assertThat(updated.getVoteStatus()).isEqualTo(Vote.VoteStatus.PENDING);
    }
  }

  @Nested
  class 실패_케이스 {

    @Test
    @DisplayName("권한 없음 - 다른 사용자가 수정 시 FORBIDDEN")
    void updateVote_forbidden_otherUser() throws Exception {
      // given
      // 다른 유저 생성
      User otherUser =
          userRepository.save(
              User.builder()
                  .nickname("other")
                  .email("other@moa.com")
                  .role(User.Role.USER)
                  .userStatus(User.UserStatus.ACTIVE)
                  .lastActiveAt(LocalDateTime.now())
                  .build());
      String otherAccessToken = jwtTokenService.issueAccessToken(otherUser.getId());
      VoteUpdateRequest request =
          new VoteUpdateRequest("수정된 본문", "", LocalDateTime.now().plusDays(3));

      // when & then
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", testVote.getId())
                  .header("Authorization", "Bearer " + otherAccessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(VoteErrorCode.FORBIDDEN.name()));
    }

    static Stream<Vote.VoteStatus> forbiddenStatuses() {
      return Stream.of(Vote.VoteStatus.OPEN, Vote.VoteStatus.CLOSED, Vote.VoteStatus.PENDING);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("forbiddenStatuses")
    @DisplayName("권한 없음 - REJECTED가 아닌 투표 상태")
    void updateVote_forbidden_status(Vote.VoteStatus status) throws Exception {
      // given
      testVote.updateModerationResult(status);
      voteRepository.save(testVote);

      VoteUpdateRequest request =
          new VoteUpdateRequest("수정된 본문", "", LocalDateTime.now().plusDays(3));

      // when & then
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(VoteErrorCode.FORBIDDEN.name()));
    }

    @Test
    @DisplayName("없는 투표 - 존재하지 않는 voteId")
    void updateVote_voteNotFound() throws Exception {
      // given
      Long notExistVoteId = 999999L;
      VoteUpdateRequest request =
          new VoteUpdateRequest("수정된 본문", "", LocalDateTime.now().plusDays(3));

      // when & then
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", notExistVoteId)
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(VoteErrorCode.VOTE_NOT_FOUND.name()));
    }

    static Stream<Arguments> invalidContents() {
      return Stream.of(Arguments.of("", "빈 문자열"), Arguments.of("a".repeat(101), "100자 초과"));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidContents")
    @DisplayName("본문 유효성 검증 실패")
    void updateVote_invalidContent_tooLong(String content, String testName) throws Exception {
      // given
      VoteUpdateRequest request =
          new VoteUpdateRequest(content, "", LocalDateTime.now().plusDays(3));

      // when & then
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(VoteErrorCode.INVALID_CONTENT.name()));
    }

    static Stream<Arguments> invalidClosedAts() {
      return Stream.of(
          Arguments.of(LocalDateTime.now().minusDays(1), "종료일이 과거시각"),
          Arguments.of(LocalDateTime.now(), "종료일이 현재시각"));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidClosedAts")
    @DisplayName("종료일 유효성 검증 실패")
    void updateVote_invalidClosedAt(LocalDateTime closedAt, String testName) throws Exception {
      // given
      VoteUpdateRequest request = new VoteUpdateRequest("수정된 본문", "", closedAt);

      // when & then
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(VoteErrorCode.INVALID_TIME.name()));
    }
  }
}
