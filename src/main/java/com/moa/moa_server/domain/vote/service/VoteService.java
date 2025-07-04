package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.global.util.XssUtil;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.util.GroupLookupHelper;
import com.moa.moa_server.domain.image.model.ImageProcessResult;
import com.moa.moa_server.domain.image.service.ImageService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.request.VoteUpdateRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteDeleteResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteModerationReasonResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteUpdateResponse;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.dto.response.result.VoteResultResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteModerationLog;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteModerationLogRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultRedisService;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import com.moa.moa_server.domain.vote.util.VoteValidator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final VoteResponseRepository voteResponseRepository;
  private final VoteModerationLogRepository voteModerationLogRepository;

  private final GroupLookupHelper groupLookupHelper;
  private final VoteResultService voteResultService;
  private final VoteModerationService voteModerationService;
  private final VoteResultRedisService voteResultRedisService;
  private final ImageService imageService;
  private final VoteRelatedDataCleaner voteRelatedDataCleaner;

  @Transactional
  public Long createVote(Long userId, VoteCreateRequest request) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 그룹 조회
    Group group =
        groupRepository
            .findById(request.groupId())
            .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));

    // 멤버십 확인
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
    ZonedDateTime koreaTime = request.closedAt().atZone(ZoneId.of("Asia/Seoul"));
    LocalDateTime utcTime = koreaTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    VoteValidator.validateUserVoteClosedAt(utcTime);

    // VoteStatus 결정 (prod 환경에서만)
    Vote.VoteStatus status =
        "prod".equals(activeProfile) ? Vote.VoteStatus.PENDING : Vote.VoteStatus.OPEN;

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
    if ("prod".equals(activeProfile)) {
      voteModerationService.requestModeration(vote.getId(), vote.getContent());
    }

    return vote.getId();
  }

  @Transactional
  public void submitVote(Long userId, Long voteId, VoteSubmitRequest request) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

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

    // 투표 권한 조회
    Group group = vote.getGroup();
    if (!group.isPublicGroup()) {
      boolean isGroupMember = groupMemberRepository.findByGroupAndUser(group, user).isPresent();

      if (!isGroupMember) {
        throw new VoteException(VoteErrorCode.NOT_GROUP_MEMBER);
      }
    }

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
  }

  @Transactional
  public VoteDetailResponse getVoteDetail(Long userId, Long voteId) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 투표 조회
    Vote vote = findVoteOrThrow(voteId);

    // 상태/권한 검사
    validateVoteContentReadable(vote, userId);

    return new VoteDetailResponse(
        vote.getId(),
        vote.getGroup().getId(),
        vote.getGroup().getName(),
        vote.isAnonymous() ? "익명" : vote.getUser().getNickname(),
        vote.getContent(),
        vote.getImageUrl(),
        vote.getImageName(),
        vote.getCreatedAt(),
        vote.getClosedAt(),
        vote.isAnonymous() ? 0 : (vote.isAdminVote() ? 1 : 0));
  }

  @Transactional
  public VoteResultResponse getVoteResult(Long userId, Long voteId) {
    // 유저 조회 및 유효성 검사
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 투표 조회
    Vote vote = findVoteOrThrow(voteId);

    // 상태/권한 검사
    validateVoteReadable(vote);
    validateVoteAccess(user, vote);

    // 사용자 응답 조회 (없으면 null)
    Optional<VoteResponse> userVoteResponse = voteResponseRepository.findByVoteAndUser(vote, user);
    Integer userResponse = userVoteResponse.map(VoteResponse::getOptionNumber).orElse(null);

    // 전체 참여자 수 계산
    List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);
    int totalCount = (int) responses.stream().filter(vr -> vr.getOptionNumber() > 0).count();

    // 결과 조회
    List<VoteOptionResult> results = voteResultService.getResults(vote);

    return new VoteResultResponse(voteId, userResponse, totalCount, results);
  }

  @Transactional(readOnly = true)
  public VoteModerationReasonResponse getModerationReason(Long userId, Long voteId) {
    // 유저 조회 및 검증
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    Vote vote = findVoteOrThrow(voteId);

    // 조회 권한 검증 - 등록자여야 함
    if (!vote.getUser().getId().equals(userId)) {
      throw new VoteException(VoteErrorCode.FORBIDDEN);
    }

    VoteModerationLog log =
        voteModerationLogRepository
            .findFirstByVote_IdOrderByCreatedAtDesc(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.MODERATION_LOG_NOT_FOUND));
    return new VoteModerationReasonResponse(voteId, log.getReviewReason().name());
  }

  @Transactional
  public VoteUpdateResponse updateVote(Long userId, Long voteId, VoteUpdateRequest request) {
    // 1. 유저/투표 조회 및 권한 체크
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));

    if (!vote.getUser().getId().equals(userId)) throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 2. 상태/수정 가능 체크 (REJECTED 상태만 수정 가능)
    if (vote.getVoteStatus() != Vote.VoteStatus.REJECTED)
      throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 3. 본문, 종료 시각 유효성 검사
    VoteValidator.validateContent(request.content());
    ZonedDateTime koreaTime = request.closedAt().atZone(ZoneId.of("Asia/Seoul"));
    LocalDateTime utcTime = koreaTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    VoteValidator.validateUserVoteClosedAt(utcTime);

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
    vote.updateForEdit(sanitizedContent, imageResult.imageUrl(), imageResult.imageName(), utcTime);
    voteRepository.save(vote);

    // 6. AI 서버로 검열 요청 (prod 환경에서만)
    if ("prod".equals(activeProfile)) {
      voteModerationService.requestModeration(vote.getId(), vote.getContent());
    }

    return new VoteUpdateResponse(vote.getId());
  }

  @Transactional
  public VoteDeleteResponse deleteVote(Long userId, Long voteId) {
    // 1. 유저/투표 조회 및 권한 체크
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));

    if (!vote.getUser().getId().equals(userId)) throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 2. 삭제 가능 체크 (REJECTED 상태만 수정 가능)
    if (vote.getVoteStatus() != Vote.VoteStatus.REJECTED)
      throw new VoteException(VoteErrorCode.FORBIDDEN);

    // 3. 투표 삭제
    vote.softDelete();

    // 4. 관련 데이터 정리 (존재할 수 있는 경우)
    voteRelatedDataCleaner.cleanup(voteId);

    return new VoteDeleteResponse(voteId);
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

  /** 투표 내용 조회 가능 상태 및 권한 검사 */
  private void validateVoteContentReadable(Vote vote, Long userId) {
    if (vote.getVoteStatus() == Vote.VoteStatus.OPEN
        || vote.getVoteStatus() == Vote.VoteStatus.CLOSED) {
      return;
    }
    if (vote.getVoteStatus() == Vote.VoteStatus.REJECTED && vote.getUser().getId().equals(userId)) {
      return;
    }
    throw new VoteException(VoteErrorCode.FORBIDDEN);
  }

  /** 투표 조회 가능한 상태인지 검사 (내용, 결과, 댓글 읽기) 허용 상태: OPEN, CLOSED */
  private void validateVoteReadable(Vote vote) {
    if (vote.getVoteStatus() != Vote.VoteStatus.OPEN
        && vote.getVoteStatus() != Vote.VoteStatus.CLOSED) {
      throw new VoteException(VoteErrorCode.FORBIDDEN);
    }
  }

  /** 투표 조회 권한 검사 (내용, 결과, 댓글 읽기) 조건: 등록자이거나 참여자(기권 불가)여야 함 * top3 투표는 추후 그룹 멤버 여부로도 허용 예정 */
  private void validateVoteAccess(User user, Vote vote) {
    if (isVoteAuthor(user, vote)) return;
    if (hasParticipatedWithValidOption(user, vote)) return;

    // TODO: top3 투표일 경우 isGroupMember 검사 후 허용
    throw new VoteException(VoteErrorCode.FORBIDDEN);
  }

  private boolean isVoteAuthor(User user, Vote vote) {
    return vote.getUser().equals(user);
  }

  private boolean hasParticipatedWithValidOption(User user, Vote vote) {
    return voteResponseRepository
        .findByVoteAndUser(vote, user)
        .map(vr -> vr.getOptionNumber() > 0)
        .orElse(false);
  }

  public void deleteVoteByGroupId(Long groupId) {
    // 1. 해당 그룹의 모든 투표 soft delete
    voteRepository.softDeleteByGroupId(groupId);

    // 2. 각 투표의 연관 데이터 정리
    List<Long> voteIds = voteRepository.findAllIdsByGroupId(groupId);
    for (Long voteId : voteIds) {
      voteRelatedDataCleaner.cleanup(voteId);
    }
  }
}
