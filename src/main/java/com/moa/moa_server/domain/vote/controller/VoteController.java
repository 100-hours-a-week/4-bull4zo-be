package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequestDto;
import com.moa.moa_server.domain.vote.dto.response.VoteCreateResponseDto;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
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
            @RequestBody VoteCreateRequestDto request
    ) {
        // 투표 등록 로직 수행
        Long voteId = voteService.createVote(userId, request);

        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", new VoteCreateResponseDto(voteId)));
    }
}
