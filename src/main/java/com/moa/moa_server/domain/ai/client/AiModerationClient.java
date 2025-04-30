package com.moa.moa_server.domain.ai.client;

import com.moa.moa_server.domain.ai.dto.ModerationRequest;
import com.moa.moa_server.domain.ai.dto.ModerationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiModerationClient {

    private final RestTemplate restTemplate;

    @Value("${ai.server-url}")
    private String aiServerUrl;

    public ModerationResponse requestModeration(ModerationRequest request) {
        try {
            String moderationUrl = aiServerUrl + "/api/v1/moderation";
            return restTemplate.postForObject(moderationUrl, request, ModerationResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 서버 요청 실패", e);
        }
    }
}