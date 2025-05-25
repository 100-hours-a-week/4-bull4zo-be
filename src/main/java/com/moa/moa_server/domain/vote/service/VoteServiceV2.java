package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.global.cursor.ClosedAtVoteIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.service.GroupService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.request.VoteSubmitRequest;
import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteItem;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.repository.VoteResultRepository;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VoteServiceV2 {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final VoteResponseRepository voteResponseRepository;
  private final VoteResultRepository voteResultRepository;
  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final GroupRepository groupRepository;

  private final GroupService groupService;

  @Transactional
  public void submitVote(Long voteId, Long userId, VoteSubmitRequest request) {
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
    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));

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

    // 1. 응답 저장
    VoteResponse voteResponse = VoteResponse.create(vote, user, response);
    try {
      voteResponseRepository.save(voteResponse);
    } catch (DataIntegrityViolationException e) {
      throw new VoteException(VoteErrorCode.ALREADY_VOTED);
    }

    // 2. 집계 테이블 업데이트
    if (response == 1 || response == 2) {
      int updated = voteResultRepository.incrementOptionCount(voteId, response);
      if (updated != 1) {
        throw new VoteException(VoteErrorCode.RESULT_UPDATE_FAIL);
      }
    }
  }

  @Transactional(readOnly = true)
  public SubmittedVoteResponse getSubmittedVotes(
      Long userId, @Nullable Long groupId, @Nullable String cursor, @Nullable Integer size) {

    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    ClosedAtVoteIdCursor parsedCursor = cursor != null ? ClosedAtVoteIdCursor.parse(cursor) : null;
    if (cursor != null && !voteRepository.existsById(parsedCursor.voteId())) {
      throw new VoteException(VoteErrorCode.VOTE_NOT_FOUND);
    }

    // 유저 조회 및 검증
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);

    // 조회 대상 그룹 목록 수집
    List<Group> groups;
    if (groupId != null) {
      // 특정 그룹이 지정된 경우
      Group group =
          groupRepository
              .findById(groupId)
              .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
      groups = List.of(group);
    } else {
      // 전체 그룹 조회: 유저가 속한 그룹 + 공개 그룹
      groups = groupMemberRepository.findAllActiveGroupsByUser(user);
      Group publicGroup = groupService.getPublicGroup();
      if (!groups.contains(publicGroup)) {
        groups.add(publicGroup);
      }
    }

    // 참여한 투표 목록 조회
    List<Vote> votes = voteRepository.findSubmittedVotes(user, groups, parsedCursor, pageSize + 1);

    // 응답 구성
    boolean hasNext = votes.size() > pageSize;
    if (hasNext) votes = votes.subList(0, pageSize);

    String nextCursor =
        votes.isEmpty()
            ? null
            : new ClosedAtVoteIdCursor(votes.getLast().getClosedAt(), votes.getLast().getId())
                .encode();

    // 각 투표별 집계 결과를 포함한 응답 DTO 구성
    List<SubmittedVoteItem> items =
        votes.stream()
            .map(
                vote -> {
                  var results =
                      voteResultRepository.findAllByVoteId(vote.getId()).stream()
                          .map(
                              vr ->
                                  new VoteOptionResultWithId(
                                      vote.getId(),
                                      vr.getOptionNumber(),
                                      vr.getCount(),
                                      vr.getRatio().doubleValue()))
                          .toList();
                  return SubmittedVoteItem.from(vote, results);
                })
            .toList();
    return new SubmittedVoteResponse(items, nextCursor, hasNext, items.size());
  }
}
