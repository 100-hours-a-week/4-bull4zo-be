package com.moa.moa_server.domain.ai.mock;

import com.moa.moa_server.domain.ai.dto.ModerationRequest;
import com.moa.moa_server.domain.ai.dto.ModerationResponse;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@ConditionalOnProperty(name = "mock.ai.enabled", havingValue = "true")
@RestController
@RequestMapping("/mock-ai/api/v1")
public class MockAiController {

  @PostMapping("/moderation")
  public ResponseEntity<ModerationResponse> mockModeration(@RequestBody ModerationRequest request) {
    if (request.content() == null || request.content().isBlank()) {
      return ResponseEntity.badRequest()
          .body(
              new ModerationResponse(
                  "Bad Request", "voteContent must not be null, empty, or whitespace only.", null));
    }
    return ResponseEntity.ok(
        new ModerationResponse("Accepted", "voteContent has been queued", null));
  }
}
