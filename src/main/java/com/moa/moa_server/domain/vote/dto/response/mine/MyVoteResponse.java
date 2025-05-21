package com.moa.moa_server.domain.vote.dto.response.mine;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "내가 만든 투표 목록 조회 응답 DTO")
public record MyVoteResponse(
    @Schema(description = "투표 목록") List<MyVoteItem> votes,
    @Schema(
            description = "현재 페이지의 마지막 항목 기준 커서",
            example = "2025-04-21T12:00:00_2025-04-20T12:00:00")
        String nextCursor,
    @Schema(description = "다음 페이지 여부", example = "false") boolean hasNext,
    @Schema(description = "현재 받아온 리스트 길이", example = "1") int size) {
  public static MyVoteResponse of(List<MyVoteItem> votes, String nextCursor, boolean hasNext) {
    return new MyVoteResponse(votes, nextCursor, hasNext, votes.size());
  }
}
