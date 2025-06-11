package com.moa.moa_server.integration.comment;

import static com.moa.moa_server.util.TestFixture.*;
import static com.moa.moa_server.util.TestFixture.comment;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.moa.moa_server.domain.auth.service.JwtTokenService;
import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentPollingIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired GroupRepository groupRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired JwtTokenService jwtTokenService;
  @Autowired CommentRepository commentRepository;
  @Autowired EntityManager em;

  User testUser;
  Group testGroup;
  Vote testVote;
  String accessToken;

  @BeforeEach
  void setup() {
    String suffix = UUID.randomUUID().toString().substring(0, 6);
    testUser = userRepository.save(user("user_" + suffix));
    testGroup = groupRepository.save(group(testUser, "group_" + suffix));
    testVote = voteRepository.save(vote(testUser, testGroup, Vote.VoteStatus.OPEN));
    accessToken = jwtTokenService.issueAccessToken(testUser.getId());
    // 댓글 미리 생성 X
  }

  @Nested
  class 성공_케이스 {

    @Test
    @DisplayName("새 댓글이 중간에 생기는 경우")
    void pollComments_newComment_arrives() throws Exception {
      // 1. 별도 쓰레드에서 2초 뒤 댓글 삽입
      new Thread(
              () -> {
                try {
                  Thread.sleep(2000);
                  commentRepository.save(comment(testVote, testUser, "테스트 댓글", 0));
                  em.flush();
                  em.clear();
                } catch (Exception ignored) {
                }
              })
          .start();

      // 2. 롱폴링 요청
      MvcResult asyncListener =
          mockMvc
              .perform(
                  get("/api/v1/votes/{voteId}/comments/poll", testVote.getId())
                      .header(
                          "Authorization",
                          "Bearer " + accessToken) // 처음엔 커서 없음 (댓글 데이터가 아예 없는 경우에서 조회하는 경우)
                  )
              .andExpect(request().asyncStarted()) // 비동기 시작 확인
              .andReturn();

      asyncListener.getAsyncResult(12000);

      mockMvc
          .perform(asyncDispatch(asyncListener))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.comments.length()").value(1))
          .andExpect(jsonPath("$.data.comments[0].content").value("테스트 댓글"))
          .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    @DisplayName("TIMEOUT 동안 새 댓글이 없으면 빈 목록 응답")
    void pollComments_timeout_noNewComment() throws Exception {
      // 1. 롱폴링 요청
      MvcResult asyncListener =
          mockMvc
              .perform(
                  get("/api/v1/votes/{voteId}/comments/poll", testVote.getId())
                      .header("Authorization", "Bearer " + accessToken))
              .andExpect(request().asyncStarted()) // 비동기 시작 확인
              .andReturn();

      // 2. TIMEOUT (내부 값은 10초, controller 응답 타임아웃은 11초)
      asyncListener.getAsyncResult(12000);

      // 3. 결과 응답 검증 - 빈 목록
      mockMvc
          .perform(asyncDispatch(asyncListener))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.comments").isArray())
          .andExpect(jsonPath("$.data.comments").isEmpty())
          .andExpect(jsonPath("$.data.size").value(0));
    }

    @Test
    @DisplayName("커서가 있는 경우 커서 이후 댓글만 수신")
    void pollComments_cursorBasedFilter() throws Exception {
      // 1. 기존 댓글 1개 저장
      Comment oldComment = commentRepository.save(comment(testVote, testUser, "이전 댓글", 0));
      String cursor = oldComment.getCreatedAt().toString() + "_" + oldComment.getId();

      // 2. 새 댓글 1개는 별도 쓰레드에서 2초 후 삽입
      new Thread(
              () -> {
                try {
                  Thread.sleep(2000);
                  commentRepository.save(comment(testVote, testUser, "새 댓글", 0));
                  em.flush();
                  em.clear();
                } catch (Exception ignored) {
                }
              })
          .start();

      // 3. 커서 기반 롱폴링 요청
      MvcResult asyncListener =
          mockMvc
              .perform(
                  get("/api/v1/votes/{voteId}/comments/poll", testVote.getId())
                      .param("cursor", cursor)
                      .header("Authorization", "Bearer " + accessToken))
              .andExpect(request().asyncStarted())
              .andReturn();

      asyncListener.getAsyncResult(12000);

      // 4. 응답: 이전 댓글은 포함되지 않아야 함
      mockMvc
          .perform(asyncDispatch(asyncListener))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.comments.length()").value(1))
          .andExpect(jsonPath("$.data.comments[0].content").value("새 댓글"))
          .andExpect(jsonPath("$.data.size").value(1));
    }
  }

  @Nested
  class 실패_케이스 {

    @Test
    @DisplayName("권한 없는 사용자가 요청할 경우 FORBIDDEN - 투표에 참여하지 않은 사용자")
    void pollComments_forbidden_user() throws Exception {
      // 1. 다른 사용자 생성 (투표에 참여하지 않음)
      User stranger = userRepository.save(user("stranger"));
      String strangerToken = jwtTokenService.issueAccessToken(stranger.getId());

      // 2. 롱폴링 요청 (참여하지 않은 사용자)
      MvcResult asyncListener =
          mockMvc
              .perform(
                  get("/api/v1/votes/{voteId}/comments/poll", testVote.getId())
                      .header("Authorization", "Bearer " + strangerToken))
              .andExpect(request().asyncStarted())
              .andReturn();

      asyncListener.getAsyncResult(12000);

      // 3. 결과: 403 FORBIDDEN
      mockMvc
          .perform(asyncDispatch(asyncListener))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.FORBIDDEN.name()));
    }
  }
}
