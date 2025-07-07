package com.moa.moa_server.domain.groupanalysis.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.groupanalysis.dto.GroupAnalysisResponse;
import com.moa.moa_server.domain.groupanalysis.entity.GroupAnalysis;
import com.moa.moa_server.domain.groupanalysis.handler.GroupAnalysisErrorCode;
import com.moa.moa_server.domain.groupanalysis.handler.GroupAnalysisException;
import com.moa.moa_server.domain.groupanalysis.mongo.GroupAnalysisContent;
import com.moa.moa_server.domain.groupanalysis.repository.GroupAnalysisJpaRepository;
import com.moa.moa_server.domain.groupanalysis.repository.GroupAnalysisMongoRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupAnalysisQueryService {

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final GroupAnalysisJpaRepository analysisJpaRepository;
  private final GroupAnalysisMongoRepository analysisMongoRepository;
  private final VoteResponseRepository voteResponseRepository;

  @Transactional(readOnly = true)
  public GroupAnalysisResponse getAnalysis(Long groupId, Long userId) {
    // 유저 및 그룹 유효성 검사
    User user = validateAndGetUser(userId);
    Group group = validateAndGetGroup(groupId);

    // 권한 검사 (그룹 멤버)
    validateGroupMember(group, user);

    // 참여율 계산 (현재 시간 기준 일주일간 참여율)
    LocalDateTime todayStart = LocalDateTime.now(ZoneOffset.UTC);
    LocalDateTime oneWeekAgo = todayStart.minusWeeks(7);
    GroupAnalysisResponse.ParticipationStats participationStats =
        calculateParticipationStats(group, oneWeekAgo, todayStart);

    // 가장 최근 분석 메타데이터 조회
    GroupAnalysis analysis = getLatestAnalysisMeta(group.getId());

    if (analysis == null) {
      // 분석이 없으면 빈 응답
      return GroupAnalysisResponse.empty(group, oneWeekAgo, participationStats);
    }

    // 분석이 있는 경우
    GroupAnalysisContent content = getAnalysisContentBy(analysis); // MongoDB 조회
    return GroupAnalysisResponse.from(group, analysis, content, participationStats);
  }

  private User validateAndGetUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  private Group validateAndGetGroup(Long groupId) {
    // 공개 그룹은 분석 조회 대상 아님
    if (groupId.equals(1L)) {
      throw new GroupAnalysisException(GroupAnalysisErrorCode.PUBLIC_GROUP_FORBIDDEN);
    }
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupAnalysisException(GroupAnalysisErrorCode.GROUP_NOT_FOUND));
  }

  private void validateGroupMember(Group group, User user) {
    boolean isMember = groupMemberRepository.existsByGroupAndUser(group, user);
    if (!isMember) {
      throw new GroupAnalysisException(GroupAnalysisErrorCode.NOT_GROUP_MEMBER);
    }
  }

  // 분석 조회
  private GroupAnalysis getLatestAnalysisMeta(Long groupId) {
    return analysisJpaRepository.findTopByGroupIdOrderByPublishedAtDesc(groupId).orElse(null);
  }

  private GroupAnalysisContent getAnalysisContentBy(GroupAnalysis analysis) {
    return analysisMongoRepository.findByAnalysisId(analysis.getId()).orElse(null);
  }

  // 참여율 계산
  private GroupAnalysisResponse.ParticipationStats calculateParticipationStats(
      Group group, LocalDateTime start, LocalDateTime end) {
    // 전체 멤버 수
    int totalMembers = groupMemberRepository.countByGroup(group);
    if (totalMembers == 0) {
      return new GroupAnalysisResponse.ParticipationStats(0.0, 0.0);
    }

    // 주어진 기간 동안 투표 참여한 유저 수
    int participatedMembers =
        voteResponseRepository.countDistinctUserIdsByGroupAndPeriod(group.getId(), start, end);

    double rawParticipated = (double) participatedMembers / totalMembers * 100.0;
    double participated = roundOneDecimal(Math.min(rawParticipated, 100.0));
    double notParticipated = roundOneDecimal(100.0 - participated);
    return new GroupAnalysisResponse.ParticipationStats(participated, notParticipated);
  }

  private double roundOneDecimal(double value) {
    return Math.round(value * 10.0) / 10.0;
  }
}
