package com.moa.moa_server.domain.vote.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.global.dto.ApiResponseVoid;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.dto.response.*;
import com.moa.moa_server.domain.vote.dto.response.result.VoteResultResponse;
import com.moa.moa_server.domain.vote.service.VoteListService;
import com.moa.moa_server.domain.vote.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Vote", description = "투표 도메인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/votes")
public class VoteController {

  private final VoteService voteService;
  private final VoteListService voteListService;

  @Operation(summary = "투표 등록", description = "지정한 그룹 또는 전체 공개로 새 투표를 생성합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<VoteCreateResponse>> createVote(
      @AuthenticationPrincipal Long userId, @RequestBody VoteCreateRequest request) {
    // 투표 등록 로직 수행
    Long voteId = voteService.createVote(userId, request);

    return ResponseEntity.status(201)
        .body(new ApiResponse<>("SUCCESS", new VoteCreateResponse(voteId)));
  }

  @Operation(
      summary = "투표 참여",
      description = "투표 항목을 선택하여 진행 중인 투표에 참여합니다.",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = ApiResponseVoid.class)))
      })
  @PostMapping("/{voteId}/submit")
  public ResponseEntity<ApiResponse<Void>> submitVote(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestBody VoteSubmitRequest request) {
    voteService.submitVote(userId, voteId, request);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", null));
  }

  @Operation(summary = "투표 내용 조회", description = "투표 내용, 등록자, 종료 시각 등 투표 정보를 조회합니다.")
  @GetMapping("/{voteId}")
  public ResponseEntity<ApiResponse<VoteDetailResponse>> getVoteDetail(
      @AuthenticationPrincipal Long userId, @PathVariable Long voteId) {
    VoteDetailResponse response = voteService.getVoteDetail(userId, voteId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "투표 결과 조회", description = "투표의 전체 응답 수, 각 항목에 대한 응답 수와 비율 등 투표 결과를 조회합니다.")
  @GetMapping("/{voteId}/result")
  public ResponseEntity<ApiResponse<VoteResultResponse>> getVoteResult(
      @AuthenticationPrincipal Long userId, @PathVariable Long voteId) {
    VoteResultResponse response = voteService.getVoteResult(userId, voteId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "투표 검열 사유 조회", description = "등록 실패한 투표의 검열 사유를 조회합니다.")
  @GetMapping("/{voteId}/review")
  public ResponseEntity<ApiResponse<VoteModerationReasonResponse>> getModerationReason(
      @AuthenticationPrincipal Long userId, @PathVariable Long voteId) {
    VoteModerationReasonResponse response = voteService.getModerationReason(userId, voteId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "투표 수정", description = "등록 실패한 투표의 내용을 수정합니다.")
  @PatchMapping("/{voteId}")
  public ResponseEntity<ApiResponse<VoteUpdateResponse>> updateVote(
      @AuthenticationPrincipal Long userId,
      @PathVariable Long voteId,
      @RequestBody VoteUpdateRequest request) {
    VoteUpdateResponse response = voteService.updateVote(userId, voteId, request);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }

  @Operation(summary = "투표 삭제", description = "등록 실패한 투표를 삭제합니다.")
  @DeleteMapping("/{voteId}")
  public ResponseEntity<ApiResponse<VoteDeleteResponse>> deleteVote(
      @AuthenticationPrincipal Long userId, @PathVariable Long voteId) {
    VoteDeleteResponse response = voteService.deleteVote(userId, voteId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
