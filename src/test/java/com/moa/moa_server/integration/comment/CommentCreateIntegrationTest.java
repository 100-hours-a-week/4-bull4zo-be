package com.moa.moa_server.integration.comment;

import static com.moa.moa_server.util.TestFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.moa.moa_server.domain.auth.service.JwtTokenService;
import com.moa.moa_server.domain.comment.dto.request.CommentCreateRequest;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.util.TestUtil;
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
public class CommentCreateIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired VoteResponseRepository voteResponseRepository;
  @Autowired JwtTokenService jwtTokenService;

  User testUser;
  Group testGroup;
  Vote testVote;
  String accessToken;

  @BeforeEach
  void setup() {
    testUser = userRepository.save(user("testuser"));
    testGroup = groupRepository.save(group(testUser, "공개"));
    testVote = voteRepository.save(vote(testUser, testGroup, Vote.VoteStatus.OPEN));
    accessToken = jwtTokenService.issueAccessToken(testUser.getId());
  }

  @Nested
  class 성공_케이스 {

    @Test
    @DisplayName("투표 등록자 댓글 작성 (실명 댓글)")
    void createComment_success_writer() throws Exception {
      // given
      CommentCreateRequest req = new CommentCreateRequest("댓글 내용", false);

      // when & then
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.message").value("SUCCESS"))
          .andExpect(jsonPath("$.data.content").value("댓글 내용"))
          .andExpect(jsonPath("$.data.authorNickname").value(testUser.getNickname()));
    }

    @Test
    @DisplayName("투표 참여자 댓글 작성 (실명 댓글)")
    void createComment_success_participant() throws Exception {
      // given: 투표 참여자 유저 추가
      User participant = userRepository.save(user("participant"));
      String participantToken = jwtTokenService.issueAccessToken(participant.getId());

      voteResponseRepository.save(voteResponse(testVote, participant, 1));

      CommentCreateRequest req = new CommentCreateRequest("참여자 댓글", false);

      // when & then
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + participantToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.message").value("SUCCESS"))
          .andExpect(jsonPath("$.data.content").value("참여자 댓글"))
          .andExpect(jsonPath("$.data.authorNickname").value(participant.getNickname()));
    }
  }

  @Nested
  class 성공_케이스_익명 {

    @Test
    @DisplayName("동일 유저가 익명 댓글 작성 시 익명 번호 유지")
    void createComment_anonymous_same_user_twice() throws Exception {
      // given
      CommentCreateRequest req1 = new CommentCreateRequest("첫 번째 익명", true);
      CommentCreateRequest req2 = new CommentCreateRequest("두 번째 익명", true);

      // when & then: 첫 번째 익명 댓글
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req1)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명1"));

      // 두 번째 익명 댓글
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req2)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명1"));
    }

    @Test
    @DisplayName("사용자 A, B, A가 익명 댓글 작성 시 익명1, 익명2, 익명1")
    void createComment_anonymous_A_B_A() throws Exception {
      // given: 유저 B 추가
      User userB = userRepository.save(user("userB"));
      String tokenB = jwtTokenService.issueAccessToken(userB.getId());

      voteResponseRepository.save(voteResponse(testVote, userB, 1));

      CommentCreateRequest reqA1 = new CommentCreateRequest("A 첫 댓글", true);
      CommentCreateRequest reqB = new CommentCreateRequest("B 댓글", true);
      CommentCreateRequest reqA2 = new CommentCreateRequest("A 두번째 댓글", true);

      // when & then: A(익명1)
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(reqA1)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명1"));

      // B(익명2)
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + tokenB)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(reqB)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명2"));

      // 다시 A(익명1)
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(reqA2)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명1"));
    }

    @Test
    @DisplayName("A가 익명 댓글, 실명 댓글 차례로 작성 시 익명1, 실명 닉네임 반환")
    void createComment_anonymous_and_realname_mixed() throws Exception {
      // given
      CommentCreateRequest reqAnonymous = new CommentCreateRequest("익명", true);
      CommentCreateRequest reqNamed = new CommentCreateRequest("실명", false);

      // 익명 댓글
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(reqAnonymous)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value("익명1"));

      // 실명 댓글
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(reqNamed)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.authorNickname").value(testUser.getNickname()));
    }
  }

  @Nested
  class 실패_케이스 {

    @Test
    @DisplayName("option=0 응답자는 댓글 작성 권한 없음")
    void createComment_optionZero_forbidden() throws Exception {
      // given: userB 기권 응답 등록
      User userB = userRepository.save(user("userB"));
      String tokenB = jwtTokenService.issueAccessToken(userB.getId());

      voteResponseRepository.save(voteResponse(testVote, userB, 0));

      CommentCreateRequest req = new CommentCreateRequest("기권자 댓글", false);

      // when & then
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + tokenB)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.FORBIDDEN.name()));
    }

    @Test
    @DisplayName("투표 미참여자는 댓글 작성 권한 없음")
    void createComment_notParticipant_forbidden() throws Exception {
      // given: userC 미참여자
      User userC = userRepository.save(user("userC"));
      String tokenC = jwtTokenService.issueAccessToken(userC.getId());

      CommentCreateRequest req = new CommentCreateRequest("미참여자 댓글", false);

      // when & then
      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + tokenC)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req)))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.FORBIDDEN.name()));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("invalidContentProvider")
    @DisplayName("댓글 본문이 유효하지 않으면 400 반환")
    void createComment_invalidContent(String content, String testName, String expectedMessage)
        throws Exception {
      CommentCreateRequest req = new CommentCreateRequest(content, false);

      mockMvc
          .perform(
              post("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(TestUtil.asJsonString(req)))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    private static Stream<Arguments> invalidContentProvider() {
      return Stream.of(
          Arguments.of("", "빈 문자열", "BLANK_CONTENT"),
          Arguments.of("a".repeat(256), "255자 초과", "TOO_LONG_CONTENT"));
    }
  }
}
