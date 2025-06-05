package com.moa.moa_server.integration.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.moa_server.config.TestSecurityConfig;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TestSecurityConfig.class})
@AutoConfigureMockMvc
@Transactional
public class VoteUpdateIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired ObjectMapper objectMapper;

  @Nested
  class 성공_케이스 {

    @Test
    @DisplayName("투표 수정 통합 - 본인 REJECTED 투표 정상 수정")
    void updateVote_success() throws Exception {
      // given
      User user = userRepository.save(User.builder().nickname("test").role(User.Role.USER).build());
      Vote vote =
          voteRepository.save(
              Vote.createUserVote(
                  user,
                  /* group= */ null,
                  "old",
                  "vote/abc.jpg",
                  LocalDateTime.now().plusDays(2),
                  false,
                  Vote.VoteStatus.REJECTED,
                  false));
      Long voteId = vote.getId();

      VoteUpdateRequest request =
          new VoteUpdateRequest("수정된 본문", "", LocalDateTime.now().plusDays(3));

      // when
      mockMvc
          .perform(
              patch("/api/v1/votes/{voteId}", voteId)
                  .header("Authorization", "Bearer validToken") // JWT 처리 필요시 커스텀 처리
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.message").value("SUCCESS"))
          .andExpect(jsonPath("$.data.voteId").value(voteId));

      // then: 실제 DB에서 값 확인
      Vote updated = voteRepository.findById(voteId).orElseThrow();
      assertThat(updated.getContent()).isEqualTo("수정된 본문");
      assertThat(updated.getImageUrl()).isEmpty(); // 빈 문자열로 삭제 처리
      assertThat(updated.getVoteStatus()).isEqualTo(Vote.VoteStatus.PENDING);

      // S3는 어떻게 확인? S3에 진짜 반영 되는 거임? 아니면 에러는 안 나는지? 진짜 이미지 주소로 해야하는 거 아닌지? URL 검사에서 버킷 이름 검사함
    }
  }
}
