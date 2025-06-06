package com.moa.moa_server.domain.vote.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "투표 내용 조회 응답 DTO")
public record VoteDetailResponse(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표가 속한 그룹 ID", example = "1") Long groupId,
    @Schema(description = "투표 등록자 닉네임 (익명 투표인 경우, '익명'으로 전달)", example = "nickname")
        String authorNickname,
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "투표 이미지 URL (없으면 null)", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,
    @Schema(description = "투표 시작 시각", example = "2025-04-20T12:00:00") LocalDateTime createdAt,
    @Schema(description = "투표 종료 시각", example = "2025-04-21T12:00:00") LocalDateTime closedAt,
    @Schema(description = "관리자 투표 여부 (0: 일반 투표, 1: 그룹 관리자 생성 투표)", example = "0") int adminVote) {}
