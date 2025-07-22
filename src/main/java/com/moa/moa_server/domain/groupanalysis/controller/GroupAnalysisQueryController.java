package com.moa.moa_server.domain.groupanalysis.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.groupanalysis.dto.GroupAnalysisResponse;
import com.moa.moa_server.domain.groupanalysis.service.GroupAnalysisQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "GroupAnalysis", description = "그룹 분석 도메인 API")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupAnalysisQueryController {

  private final GroupAnalysisQueryService groupAnalysisService;

  @Operation(summary = "그룹 분석 데이터 조회", description = "그룹 분석 데이터를 조회합니다.")
  @GetMapping("/{groupId}/analysis")
  public ResponseEntity<ApiResponse<GroupAnalysisResponse>> getGroupAnalysis(
      @PathVariable Long groupId, @AuthenticationPrincipal Long userId) {
    GroupAnalysisResponse response = groupAnalysisService.getAnalysis(groupId, userId);
    return ResponseEntity.ok(new ApiResponse<>("SUCCESS", response));
  }
}
