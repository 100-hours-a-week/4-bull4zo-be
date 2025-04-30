package com.moa.moa_server.domain.ai.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moa.moa_server.config.TestSecurityConfig;
import com.moa.moa_server.domain.ai.dto.ModerationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@WebMvcTest(MockAiController.class)
class MockAiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("voteContent가 없으면 400 반환")
    void testBadRequest() throws Exception {
        ModerationRequest request = new ModerationRequest(2, ""); // content 없음

        mockMvc.perform(post("/mock-ai/api/v1/moderation")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("voteContent must not be null, empty, or whitespace only."));
    }

    @Test
    @DisplayName("정상 요청 시 200 반환")
    void testAccepted() throws Exception {
        ModerationRequest request = new ModerationRequest(1, "투표 내용 예시");

        mockMvc.perform(post("/mock-ai/api/v1/moderation")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("voteContent has been queued"));
    }
}