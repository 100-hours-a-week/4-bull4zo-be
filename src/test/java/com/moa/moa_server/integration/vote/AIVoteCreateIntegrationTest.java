package com.moa.moa_server.integration.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.moa_server.domain.vote.dto.ai_vote.AIVoteCreateRequest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/data-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AIVoteCreateIntegrationTest {

  private static final Long AI_USER_ID = 100L;

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private VoteRepository voteRepository;

  @Nested
  class 성공_테스트 {

    @Test
    @DisplayName("AI 생성 투표 등록 성공")
    void createAIVote_success() throws Exception {
      AIVoteCreateRequest request =
          new AIVoteCreateRequest(
              "AI가 만든 점심 투표",
              "",
              "",
              LocalDateTime.now().plusMinutes(1),
              LocalDateTime.now().plusDays(1));

      mockMvc
          .perform(
              post("/api/v1/ai/votes")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated());

      Vote saved = voteRepository.findAll().getFirst();
      assertThat(saved.getContent()).contains("점심");
      assertThat(saved.getVoteType()).isEqualTo(Vote.VoteType.AI);
      assertThat(saved.getUser().getId()).isEqualTo(AI_USER_ID);
    }
  }
}
