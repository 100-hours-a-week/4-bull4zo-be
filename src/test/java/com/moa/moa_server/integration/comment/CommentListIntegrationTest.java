package com.moa.moa_server.integration.comment;

import static com.moa.moa_server.util.TestFixture.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.moa.moa_server.domain.auth.service.JwtTokenService;
import com.moa.moa_server.domain.comment.handler.CommentErrorCode;
import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CommentListIntegrationTest {

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
  @Autowired private CommentRepository commentRepository;

  @BeforeEach
  void setup() {
    testUser = userRepository.save(user("testuser"));
    testGroup = groupRepository.save(group(testUser, "공개"));
    testVote = voteRepository.save(vote(testUser, testGroup, Vote.VoteStatus.OPEN));
    accessToken = jwtTokenService.issueAccessToken(testUser.getId());
    // 댓글 12개 등록
    for (int i = 1; i <= 12; i++) {
      commentRepository.save(comment(testVote, testUser, "댓글 " + i, 0));
    }
  }

  @Nested
  class 성공_케이스 {

    @Test
    @DisplayName("페이지네이션 기본 동작")
    void getCommentList_basicPagination() throws Exception {
      // 첫 페이지 요청 (size=10)
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.comments.length()").value(10))
          .andExpect(jsonPath("$.data.hasNext").value(true))
          .andExpect(jsonPath("$.data.nextCursor").exists())
          .andExpect(jsonPath("$.data.size").value(10));
    }

    @Test
    @DisplayName("커서 기반 다음 페이지 조회")
    void getCommentList_withCursor() throws Exception {
      System.out.println("현재 등록된 댓글 수: " + commentRepository.count());
      commentRepository
          .findAll()
          .forEach(
              c -> System.out.println(c.getId() + " " + c.getContent() + " " + c.getCreatedAt()));

      // 1. 첫 페이지 요청
      String firstPageResponse =
          mockMvc
              .perform(
                  get("/api/v1/votes/{voteId}/comments", testVote.getId())
                      .header("Authorization", "Bearer " + accessToken)
                      .param("size", "10"))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      System.out.println("첫 페이지 응답: " + firstPageResponse);

      // 2. nextCursor 파싱
      String nextCursor = JsonPath.read(firstPageResponse, "$.data.nextCursor");
      System.out.println("nextCursor: " + nextCursor);

      // 3. 두 번째 페이지 요청
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .param("cursor", nextCursor)
                  .param("size", "10"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data.comments.length()").value(2))
          .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    // 삭제된 댓글 제외 확인
    // TO DO: 댓글 삭제 기능 구현 시 작성
    //    @Test
    //    @DisplayName("삭제 댓글은 응답에 포함되지 않음")
    //    void getCommentList_deletedCommentExcluded() throws Exception {
    //      // 댓글 하나 soft delete
    //      Comment toDelete = commentRepository.findAll().getFirst();
    //      toDelete.delete(); // soft delete 메서드 구현 필요
    //      commentRepository.save(toDelete);
    //
    //      // when & then
    //      mockMvc.perform(get("/api/v1/votes/{voteId}/comments", testVote.getId())
    //          .header("Authorization", "Bearer " + accessToken))
    //        .andExpect(status().isOk())
    //        .andExpect(jsonPath("$.data.comments.length()").value(9)); // 삭제된 거 1개 빠짐
    //    }

    @Test
    @DisplayName("댓글 작성자 여부 isMine 필드 확인")
    void getCommentList_isMineField() throws Exception {
      // given
      // 본인 댓글 1개, 다른 유저 댓글 1개 추가
      User otherUser = userRepository.save(user("otheruser"));
      commentRepository.save(comment(testVote, otherUser, "다른 사람 댓글", 0));

      // when & then
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .param("size", "15"))
          .andExpect(status().isOk())
          // 본인 댓글: isMine == true
          .andExpect(jsonPath("$.data.comments[?(@.content=='댓글 1')].isMine").value(true))
          // 다른 사람 댓글: isMine == false
          .andExpect(jsonPath("$.data.comments[?(@.content=='다른 사람 댓글')].isMine").value(false));
    }
  }

  @Nested
  class 실패_케이스 {
    // 테스트 케이스
    // 권한 없음 ✅
    // 투표가 없는 경우 ✅
    // 잘못된 커서 포맷 ✅
    // size 파라미터 0, 음수, 1000

    @Test
    @DisplayName("권한 없는 유저 (투표 참여자나 투표 등록자가 아님)")
    void getCommentList_forbidden_notParticipant() throws Exception {
      // given
      User stranger = userRepository.save(user("stranger"));
      String strangerToken = jwtTokenService.issueAccessToken(stranger.getId());

      // when & then
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + strangerToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.FORBIDDEN.name()));
    }

    @Test
    @DisplayName("권한 없는 유저 (투표 참여자지만 유효하지 않은 응답(option=0, 기권))")
    void getCommentList_forbidden_invalidParticipant() throws Exception {
      // given
      User stranger = userRepository.save(user("stranger"));
      String strangerToken = jwtTokenService.issueAccessToken(stranger.getId());
      voteResponseRepository.save(voteResponse(testVote, stranger, 0));

      // when & then
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + strangerToken))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.FORBIDDEN.name()));
    }

    @Test
    @DisplayName("투표가 존재하지 않음")
    void getCommentList_notFound() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", 9999L)
                  .header("Authorization", "Bearer " + accessToken))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.VOTE_NOT_FOUND.name()));
    }

    // TO DO: 투표 삭제 기능 추가 시 작성하기
    //    @Test
    //    @DisplayName("삭제된 투표")
    //    void getCommentList_deletedVote() throws Exception {
    //      // 투표 soft delete 처리
    //
    //      mockMvc.perform(get("/api/v1/votes/{voteId}/comments", testVote.getId())
    //          .header("Authorization", "Bearer " + accessToken))
    //        .andExpect(status().isNotFound())
    //        .andExpect(jsonPath("$.message").value(CommentErrorCode.VOTE_NOT_FOUND.name()));
    //    }

    @Test
    @DisplayName("커서 포맷 에러 - 400 반환")
    void getCommentList_invalidCursor() throws Exception {
      mockMvc
          .perform(
              get("/api/v1/votes/{voteId}/comments", testVote.getId())
                  .header("Authorization", "Bearer " + accessToken)
                  .param("cursor", "invalid_cursor_format"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value(CommentErrorCode.INVALID_CURSOR_FORMAT.name()));
    }
  }
}
