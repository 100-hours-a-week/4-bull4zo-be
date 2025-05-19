package com.moa.moa_server.domain.feedback.controller;

import com.moa.moa_server.domain.feedback.dto.request.FeedbackCreateRequest;
import com.moa.moa_server.domain.feedback.service.FeedbackService;
import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.dto.ApiResponseVoid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feedback", description = "유저 피드백 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Operation(summary = "사용자 피드백 등록",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            content = @Content(schema = @Schema(implementation = ApiResponseVoid.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createFeedback(
            @AuthenticationPrincipal Long userId,
            @RequestBody FeedbackCreateRequest request
    ) {
        feedbackService.createFeedback(userId, request);
        return ResponseEntity
                .status(201)
                .body(new ApiResponse<>("SUCCESS", null));
    }
}
