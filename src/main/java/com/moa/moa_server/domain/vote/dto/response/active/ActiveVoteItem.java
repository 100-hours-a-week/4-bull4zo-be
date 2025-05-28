package com.moa.moa_server.domain.vote.dto.response.active;

import com.moa.moa_server.domain.vote.entity.Vote;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "진행 중인 투표 정보")
public record ActiveVoteItem(
    @Schema(description = "투표 ID", example = "123") Long voteId,
    @Schema(description = "투표가 속한 그룹 ID", example = "1") Long groupId,
    @Schema(description = "투표가 속한 그룹 이름", example = "공개") String groupName,
    @Schema(description = "투표 등록자 닉네임 (익명 투표인 경우, '익명'으로 전달)", example = "nickname")
        String authorNickname,
    @Schema(description = "투표 본문 내용", example = "에어컨 추우신 분?") String content,
    @Schema(description = "투표 이미지 URL (없으면 null)", example = "https://s3.amazonaws.com/....jpg")
        String imageUrl,
    @Schema(description = "투표 시작 시각", example = "2025-04-20T12:00:00") LocalDateTime createdAt,
    @Schema(description = "투표 종료 시각", example = "2025-04-21T12:00:00") LocalDateTime closedAt,
    @Schema(description = "관리자 투표 여부 (0: 일반 투표, 1: 그룹 관리자 생성 투표)", example = "0") int adminVote,
    @Schema(description = "투표 성격 (USER, AI, EVENT 중 하나)", example = "USER") String voteType) {

  public static ActiveVoteItem from(Vote vote) {
    return new ActiveVoteItem(
        vote.getId(),
        vote.getGroup().getId(),
        vote.getGroup()
            .getName(), // TODO: group.name 조회 시 N+1 쿼리 발생 가능 → fetch join 또는 DTO projection으로 최적화
        // 필요
        vote.isAnonymous() ? "익명" : vote.getUser().getNickname(),
        vote.getContent(),
        vote.getImageUrl(),
        vote.getCreatedAt(),
        vote.getClosedAt(),
        vote.isAnonymous() ? 0 : (vote.isAdminVote() ? 1 : 0),
        vote.getVoteType().name());
  }
}
