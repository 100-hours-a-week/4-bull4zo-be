package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackRequest;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackResponse;
import com.moa.moa_server.domain.vote.service.VoteModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ai")
public class VoteModerationController {

    private final VoteModerationService voteModerationService;

    @PostMapping("/votes/moderation/callback")
    public ResponseEntity<ApiResponse> callback(
            @RequestBody VoteModerationCallbackRequest request
    ) {
        VoteModerationCallbackResponse result = voteModerationService.handleCallback(request);
        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", result));
    }
}
