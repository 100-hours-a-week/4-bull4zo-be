package com.moa.moa_server.domain.feedback.controller;

import com.moa.moa_server.domain.feedback.dto.request.FeedbackCreateRequest;
import com.moa.moa_server.domain.feedback.service.FeedbackService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse> createFeedback(
            @AuthenticationPrincipal Long userId,
            @RequestBody FeedbackCreateRequest request
    ) {
        feedbackService.createFeedback(userId, request);
        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", null));
    }
}
