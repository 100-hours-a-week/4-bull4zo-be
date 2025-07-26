package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.image.model.ImageProcessResult;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.ranking.service.RankingRedisService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteDeleteResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteUpdateResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultRedisService;
import com.moa.moa_server.domain.vote.util.VoteValidator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteCommandService {

  private static final String PROFILE_PROD = "prod";

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final VoteResponseRepository voteResponseRepository;

  private final VoteCleanerService voteCleanerService;
  private final VoteModerationService voteModerationService;
  private final VoteResultRedisService voteResultRedisService;
  private final ImageService imageService;
  private final RankingRedisService rankingRedisService;

  @Transactional
  public Long createVote(Long userId, VoteCreateRequest request) {
    // 유저, 그룹 조회 및 멤버십 검사
    User user = validateAndGetUser(userId);
    Group group = validateAndGetGroup(request.groupId());
    GroupMember groupMember = validateGroupMembership(user, group);

    // 관리자 투표 여부 판단
    boolean adminVote =
        groupMember != null
            && switch (groupMember.getRole()) {
              case OWNER, MANAGER -> true;
              default -> false;
            };

    // 요청 값 유효성 검사
    VoteValidator.validateContent(request.content());
    imageService.validateImageUrl(request.imageUrl());
    String imageUrl = request.imageUrl().isBlank() ? null : request.imageUrl().trim();
    String imageName = request.imageName().isBlank() ? null : request.imageName().trim();

    // 투표 종료 시간 변환
    LocalDateTime utcTime = convertAndValidateClosedAt(request.closedAt());

    // VoteStatus 결정 (prod 환경에서만)
    Vote.VoteStatus status =
        PROFILE_PROD.equals(activeProfile) ? Vote.VoteStatus.PENDING : Vote.VoteStatus.OPEN;

    // XSS 필터링
    String sanitizedContent = XssUtil.sanitize(request.content());

    // 이미지 처리
    if (imageUrl != null) {
      imageService.moveImageFromTempToTarget(imageUrl, "vote"); // S3 이미지 처리
      imageUrl = imageUrl.replace("/temp/", "/vote/"); // DB에는 vote 경로 저장
      imageName = XssUtil.sanitize(request.imageName());
    }

    // Vote 생성 및 저장
    Vote vote =
        Vote.createUserVote(
            user,
            group,
            sanitizedContent,
            imageUrl,
            imageName,
            utcTime,
            request.anonymous(),
            status,
            adminVote);
    voteRepository.save(vote);

    // Redis 캐시 초기화 (옵션별 count 0 설정 및 만료 시간 등록)
    voteResultRedisService.setCountsWithTTL(vote.getId(), Map.of(1, 0, 2, 0), vote.getClosedAt());

    // AI 서버로 검열 요청 (prod 환경에서만)
    if (PROFILE_PROD.equals(activeProfile)) {
      voteModerationService.requestModeration(vote.getId(), vote.getContent());
    }

    return vote.getId();
  }

  @Transactional
  public VoteUpdateResponse updateVote(Long userId, Long voteId, VoteUpdateRequest request) {
    // 유저, 투표 조회 및 소유권 체크
    User user = validateAndGetUser(userId);
    Vote vote = findVoteOrThrow(voteId);
    validateVoteOwner(vote, userId);

    // 2. 상태/수정 가능 체크 (REJECTED 상태만 수정 가능)
    if (vote.getVoteStatus() != Vote.VoteStatus.REJECTED)
      throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 3. 본문, 종료 시각 유효성 검사
    VoteValidator.validateContent(request.content());
    LocalDateTime utcTime = convertAndValidateClosedAt(request.closedAt());

    // 4. 이미지 URL/이름 처리
    ImageProcessResult imageResult =
        imageService.processImageOnUpdate(
            "vote",
            vote.getImageUrl(),
            request.imageUrl(),
            vote.getImageName(),
            request.imageName());

    // 5. content, url, closedAt 업데이트 및 저장
    String sanitizedContent = XssUtil.sanitize(request.content());
    vote.update(sanitizedContent, imageResult.imageUrl(), imageResult.imageName(), utcTime);
    vote.pending();
    voteRepository.save(vote);

    // 6. AI 서버로 검열 요청 (prod 환경에서만)
    if (PROFILE_PROD.equals(activeProfile)) {
      voteModerationService.requestModeration(vote.getId(), vote.getContent());
    }

    return new VoteUpdateResponse(vote.getId());
  }

  @Transactional
  public VoteDeleteResponse deleteVote(Long userId, Long voteId) {
    // 유저, 투표 조회 및 소유권 체크
    User user = validateAndGetUser(userId);
    Vote vote = findVoteOrThrow(voteId);
    validateVoteOwner(vote, userId);

    // 2. 삭제 가능 체크 (REJECTED 상태만 수정 가능)
    if (vote.getVoteStatus() != Vote.VoteStatus.REJECTED)
      throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 3. 투표 삭제
    vote.softDelete();

    // 4. 관련 데이터 정리 (존재할 수 있는 경우)
    voteCleanerService.cleanup(voteId);

    return new VoteDeleteResponse(voteId);
  }

  @Transactional
  public void submitVote(Long userId, Long voteId, VoteSubmitRequest request) {
    // 유저 조회 및 유효성 검사
    User user = validateAndGetUser(userId);

    // 응답 값 유효성 검증
    int response = request.userResponse();
    if (response < 0 || response > 2) {
      throw new VoteException(VoteErrorCode.INVALID_OPTION);
    }

    // 투표 조회
    Vote vote = findVoteOrThrow(voteId);

    // 투표 상태 체크
    if (!vote.isOpen()) {
      throw new VoteException(VoteErrorCode.VOTE_NOT_OPENED);
    }

    // 멤버십 검사
    Group group = validateAndGetGroup(voteId);
    validateGroupMembership(user, group);

    // 중복 투표 확인
    if (voteResponseRepository.existsByVoteAndUser(vote, user)) {
      throw new VoteException(VoteErrorCode.ALREADY_VOTED);
    }

    // 투표 응답 저장
    VoteResponse voteResponse = VoteResponse.create(vote, user, response);
    try {
      voteResponseRepository.save(voteResponse);
    } catch (DataIntegrityViolationException e) {
      throw new VoteException(VoteErrorCode.ALREADY_VOTED);
    }

    // Redis에 투표 결과 반영
    voteResultRedisService.incrementOptionCount(voteId, response);
    // 랭킹 갱신을 위해 수정된 투표를 Redis ZSet에 기록
    rankingRedisService.trackUpdatedVote(voteId);
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
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
  }

  /** 투표 조회 */
  private Vote findVoteOrThrow(Long voteId) {
    return voteRepository
        .findById(voteId)
        .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
  }

  /** 그룹에 소속된 유저인지 검사 (등록/참여에 사용) */
  private GroupMember validateGroupMembership(User user, Group group) {
    if (group.isPublicGroup()) return null;

    return groupMemberRepository
        .findByGroupAndUser(group, user)
        .orElseThrow(() -> new VoteException(VoteErrorCode.NOT_GROUP_MEMBER));
  }

  /** 투표 작성자인지 검사 */
  private void validateVoteOwner(Vote vote, Long userId) {
    if (!vote.getUser().getId().equals(userId)) {
      throw new VoteException(VoteErrorCode.FORBIDDEN);
    }
  }

  private LocalDateTime convertAndValidateClosedAt(LocalDateTime closedAt) {
    ZonedDateTime koreaTime = closedAt.atZone(ZoneId.of("Asia/Seoul"));
    LocalDateTime utcTime = koreaTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    VoteValidator.validateUserVoteClosedAt(utcTime);
    return utcTime;
  }
}
