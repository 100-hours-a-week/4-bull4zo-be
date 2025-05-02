package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteCreateResponse;
import com.moa.moa_server.domain.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<ApiResponse> vote(
            @AuthenticationPrincipal Long userId,
            @RequestBody VoteCreateRequest request
    ) {
        // 투표 등록 로직 수행
        Long voteId = voteService.createVote(userId, request);

        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", new VoteCreateResponse(voteId)));
    }
}
