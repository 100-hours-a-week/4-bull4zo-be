package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.ranking.util.RankingPermissionValidator;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.response.VoteDetailResponse;
import com.moa.moa_server.domain.vote.dto.response.VoteModerationReasonResponse;
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
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteQueryService {

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final VoteResponseRepository voteResponseRepository;
  private final VoteModerationLogRepository voteModerationLogRepository;

  private final VoteResultService voteResultService;
  private final RankingPermissionValidator rankingPermissionValidator;

  @Transactional
  public VoteDetailResponse getVoteDetail(Long userId, Long voteId) {
    // 유저, 투표 조회 및 상태/권한 검사
    User user = validateAndGetUser(userId);
    Vote vote = findVoteOrThrow(voteId);
    validateVoteContentReadable(vote, user);
    validateVoteAccess(user, vote);

    return VoteDetailResponse.of(vote);
  }

  @Transactional
  public VoteResultResponse getVoteResult(Long userId, Long voteId) {
    // 유저, 투표 조회 및 상태/권한 검사
    User user = validateAndGetUser(userId);
    Vote vote = findVoteOrThrow(voteId);
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
    // 유저, 투표 조회 및 소유권 체크
    User user = validateAndGetUser(userId);
    Vote vote = findVoteOrThrow(voteId);
    validateVoteOwner(vote, userId);

    VoteModerationLog log =
        voteModerationLogRepository
            .findFirstByVote_IdOrderByCreatedAtDesc(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.MODERATION_LOG_NOT_FOUND));
    return new VoteModerationReasonResponse(voteId, log.getReviewReason().name());
  }

  private User validateAndGetUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  /** 투표 조회 */
  private Vote findVoteOrThrow(Long voteId) {
    return voteRepository
        .findById(voteId)
        .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
  }

  /** 투표 내용 조회 가능 상태 및 권한 검사 */
  private void validateVoteContentReadable(Vote vote, User user) {
    if (vote.getVoteStatus() == Vote.VoteStatus.OPEN
        || vote.getVoteStatus() == Vote.VoteStatus.CLOSED) {
      return;
    }
    if (vote.getVoteStatus() == Vote.VoteStatus.REJECTED
        && vote.getUser().getId().equals(user.getId())) {
      return;
    }
    throw new VoteException(VoteErrorCode.FORBIDDEN);
  }

  /** 투표 조회 가능한 상태인지 검사 (내용, 결과) 허용 상태: OPEN, CLOSED */
  private void validateVoteReadable(Vote vote) {
    if (vote.getVoteStatus() != Vote.VoteStatus.OPEN
        && vote.getVoteStatus() != Vote.VoteStatus.CLOSED) {
      throw new VoteException(VoteErrorCode.FORBIDDEN);
    }
  }

  /** 투표 결과 조회 권한 검사 (조건: 등록자이거나 참여자(기권 불가)여야 함) */
  private void validateVoteAccess(User user, Vote vote) {
    if (isVoteAuthor(user, vote)) return;
    if (hasParticipatedWithValidOption(user, vote)) return;
    if (rankingPermissionValidator.isAccessibleAsTopRankedVote(user, vote)) return;
    throw new VoteException(VoteErrorCode.FORBIDDEN);
  }

  /** 투표 작성자인지 검사 */
  private void validateVoteOwner(Vote vote, Long userId) {
    if (!vote.getUser().getId().equals(userId)) {
      throw new VoteException(VoteErrorCode.FORBIDDEN);
    }
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
}
