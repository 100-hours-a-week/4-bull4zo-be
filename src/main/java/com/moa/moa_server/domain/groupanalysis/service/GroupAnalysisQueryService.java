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

  @Transactional(readOnly = true)
  public GroupAnalysisResponse getAnalysis(Long groupId, Long userId) {
    // 유저 및 그룹 유효성 검사
    User user = validateAndGetUser(userId);
    Group group = validateAndGetGroup(groupId);

    // 권한 검사 (그룹 멤버)
    validateGroupMember(group, user);

    // 참여율

    // 가장 최근 분석 메타데이터 조회 (publishedAt DESC)
    GroupAnalysis analysis = getLatestAnalysisMeta(group.getId());

    // 분석 없으면 빈 응답
    if (analysis == null) {
      return GroupAnalysisResponse.empty(group);
    }

    // MongoDB 분석 데이터 조회
    GroupAnalysisContent content = getAnalysisContentBy(analysis);
    return GroupAnalysisResponse.from(group, analysis, content);
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

  private GroupAnalysis getLatestAnalysisMeta(Long groupId) {
    return analysisJpaRepository.findTopByGroupIdOrderByPublishedAtDesc(groupId).orElse(null);
  }

  private GroupAnalysisContent getAnalysisContentBy(GroupAnalysis analysis) {
    return analysisMongoRepository.findByAnalysisId(analysis.getId()).orElse(null);
  }
}
