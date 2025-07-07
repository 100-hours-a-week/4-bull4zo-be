package com.moa.moa_server.domain.groupanalysis.controller;

import com.moa.moa_server.domain.global.dto.ApiResponse;
import com.moa.moa_server.domain.groupanalysis.dto.AIGroupAnalysisCreateRequest;
import com.moa.moa_server.domain.groupanalysis.dto.AIGroupAnalysisCreateResponse;
import com.moa.moa_server.domain.groupanalysis.service.GroupAnalysisCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AIAnalysis", description = "AI 서버가 호출하는 그룹 분석 도메인 API")
@RestController
@RequestMapping("/api/v1/ai/groups/analysis")
@RequiredArgsConstructor
public class GroupAnalysisCommandController {

  private final GroupAnalysisCommandService groupAnalysisService;

  @Operation(summary = "그룹 분석 데이터 저장", description = "AI가 생성한 그룹 주간 분석 데이터를 저장합니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<AIGroupAnalysisCreateResponse>> createAIGroupAnalysis(
      @RequestBody AIGroupAnalysisCreateRequest request) {
    AIGroupAnalysisCreateResponse response = groupAnalysisService.createAIGroupAnalysis(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>("SUCCESS", response));
  }
}
