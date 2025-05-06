package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteCreateResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.dto.response.active.ActiveVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.mine.MyVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.result.VoteResultResponse;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteResponse;
import com.moa.moa_server.domain.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteController {

    private final VoteService voteService;

    @PostMapping
    public ResponseEntity<ApiResponse> createVote(
            @AuthenticationPrincipal Long userId,
            @RequestBody VoteCreateRequest request
    ) {
        // 투표 등록 로직 수행
        Long voteId = voteService.createVote(userId, request);

        return ResponseEntity
                .status(201)
                .body(new ApiResponse("SUCCESS", new VoteCreateResponse(voteId)));
    }

    @PostMapping("/{voteId}/submit")
    public ResponseEntity<ApiResponse> submitVote(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voteId,
            @RequestBody VoteSubmitRequest request
    ) {
        voteService.submitVote(userId, voteId, request);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", null));
    }

    @GetMapping("/{voteId}")
    public ResponseEntity<ApiResponse> getVoteDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voteId
    ) {
        VoteDetailResponse response = voteService.getVoteDetail(userId, voteId);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping("/{voteId}/result")
    public ResponseEntity<ApiResponse> getVoteResult(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long voteId
    ) {
        VoteResultResponse response = voteService.getVoteResult(userId, voteId);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getActiveVotes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        ActiveVoteResponse response = voteService.getActiveVotes(userId, groupId, cursor, size);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse> getMyVotes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        MyVoteResponse response = voteService.getMyVotes(userId, groupId, cursor, size);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }

    @GetMapping("/submit")
    public ResponseEntity<ApiResponse> getSubmittedVotes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size
    ) {
        SubmittedVoteResponse response = voteService.getSubmittedVotes(userId, groupId, cursor, size);
        return ResponseEntity.ok(new ApiResponse("SUCCESS", response));
    }
}
