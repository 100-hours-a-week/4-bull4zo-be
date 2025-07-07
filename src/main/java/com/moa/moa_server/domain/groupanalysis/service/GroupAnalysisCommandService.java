package com.moa.moa_server.domain.groupanalysis.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.groupanalysis.dto.AIGroupAnalysisCreateRequest;
import com.moa.moa_server.domain.groupanalysis.dto.AIGroupAnalysisCreateResponse;
import com.moa.moa_server.domain.groupanalysis.entity.GroupAnalysis;
import com.moa.moa_server.domain.groupanalysis.handler.GroupAnalysisErrorCode;
import com.moa.moa_server.domain.groupanalysis.handler.GroupAnalysisException;
import com.moa.moa_server.domain.groupanalysis.mapper.GroupAnalysisContentMapper;
import com.moa.moa_server.domain.groupanalysis.mongo.GroupAnalysisContent;
import com.moa.moa_server.domain.groupanalysis.repository.GroupAnalysisJpaRepository;
import com.moa.moa_server.domain.groupanalysis.repository.GroupAnalysisMongoRepository;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupAnalysisCommandService {

  private final GroupRepository groupRepository;
  private final GroupAnalysisJpaRepository analysisJpaRepository;
  private final GroupAnalysisMongoRepository analysisMongoRepository;

  @Transactional
  public AIGroupAnalysisCreateResponse createAIGroupAnalysis(AIGroupAnalysisCreateRequest request) {
    // 입력값 검증
    Group group =
        groupRepository
            .findById(request.groupId())
            .orElseThrow(() -> new GroupAnalysisException(GroupAnalysisErrorCode.GROUP_NOT_FOUND));

    validateWeekStartAt(request.weekStartAt());
    validateDuplicateAnalysis(group.getId(), request.weekStartAt());

    // MySQL 저장
    LocalDateTime publishedAt = calculatePublishedAt(request.weekStartAt());
    GroupAnalysis analysis = GroupAnalysis.of(group, request.weekStartAt(), publishedAt);
    GroupAnalysis saved = analysisJpaRepository.save(analysis);

    // MongoDB 저장
    GroupAnalysisContent content = GroupAnalysisContentMapper.toDocument(saved, request);
    try {
      analysisMongoRepository.save(content);
    } catch (Exception e) {
      log.error(
          "[GroupAnalysisService#createAIGroupAnalysis] MongoDB 저장 실패: groupId={}, weekStartAt={}",
          group.getId(),
          request.weekStartAt(),
          e);
      analysisJpaRepository.deleteById(saved.getId()); // 롤백
      throw new GroupAnalysisException(GroupAnalysisErrorCode.MONGO_SAVE_FAILED);
    }

    return new AIGroupAnalysisCreateResponse(group.getId(), true);
  }

  private LocalDateTime calculatePublishedAt(LocalDateTime weekStartAt) {
    // weekStartAt의 다음 주 월요일 오전 9시(KST)
    ZoneId koreaZone = ZoneId.of("Asia/Seoul");
    ZonedDateTime zoned =
        weekStartAt
            .atZone(koreaZone)
            .plusWeeks(1)
            .withHour(9)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
    return zoned.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }

  private void validateWeekStartAt(LocalDateTime weekStartAt) {
    // 현재 시점보다 이후인 시작일은 허용하지 않음
    if (weekStartAt.isAfter(LocalDateTime.now(ZoneOffset.UTC))) {
      throw new GroupAnalysisException(GroupAnalysisErrorCode.INVALID_TIME);
    }
  }

  private void validateDuplicateAnalysis(Long groupId, LocalDateTime weekStartAt) {
    // 동일 그룹(groupId)의 동일 주차(weekStartAt) 분석 데이터는 유일해야 함
    boolean exists = analysisJpaRepository.existsByGroupIdAndWeekStartAt(groupId, weekStartAt);
    if (exists) {
      throw new GroupAnalysisException(GroupAnalysisErrorCode.DUPLICATE_ANALYSIS);
    }
  }
}
