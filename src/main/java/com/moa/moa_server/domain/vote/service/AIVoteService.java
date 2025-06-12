package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.global.constant.SystemConstants;
import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.vote.dto.ai_vote.AIVoteCreateRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultRedisService;
import com.moa.moa_server.domain.vote.util.VoteValidator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIVoteService {

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;

  private final ImageService imageService;
  private final VoteResultRedisService voteResultRedisService;

  public Long createVote(AIVoteCreateRequest request) {
    // 유효성 검사
    VoteValidator.validateContent(request.content());
    imageService.validateImageUrl(request.imageUrl());

    // 시간 처리
    LocalDateTime openUtc = toUtc(request.openAt());
    LocalDateTime closedUtc = toUtc(request.closedAt());
    VoteValidator.validateOpenAt(openUtc);
    VoteValidator.validateAIVoteClosedAt(openUtc, closedUtc);

    // 이미지 처리
    String imageUrl = processImageUrl(request.imageUrl());
    String imageName = processImageName(request.imageName());

    // 시스템 유저 / 공개 그룹 조회
    User systemUser = getSystemUser();
    Group publicGroup = getPublicGroup();

    // Vote 생성 및 저장
    Vote vote =
        Vote.createAIVote(
            request.content(), imageUrl, imageName, openUtc, closedUtc, systemUser, publicGroup);

    voteRepository.save(vote);
    voteResultRedisService.setCountsWithTTL(vote.getId(), Map.of(1, 0, 2, 0), vote.getClosedAt());

    return vote.getId();
  }

  // ===== 내부 유틸 메서드 ===

  private LocalDateTime toUtc(LocalDateTime localTime) {
    return localTime
        .atZone(ZoneId.of("Asia/Seoul"))
        .withZoneSameInstant(ZoneOffset.UTC)
        .toLocalDateTime();
  }

  private String processImageUrl(String url) {
    if (url == null || url.isBlank()) return null;
    imageService.moveImageFromTempToTarget(url, "vote");
    return url.replace("/temp/", "/vote/");
  }

  private String processImageName(String name) {
    if (name == null || name.isBlank()) return null;
    return XssUtil.sanitize(name.trim());
  }

  private User getSystemUser() {
    return userRepository
        .findById(SystemConstants.SYSTEM_USER_ID)
        .orElseThrow(() -> new IllegalStateException("System user not found"));
  }

  private Group getPublicGroup() {
    return groupRepository
        .findById(SystemConstants.PUBLIC_GROUP_ID)
        .orElseThrow(() -> new IllegalStateException("Public group not found"));
  }
}
