package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.comment.service.CommentCountService;
import com.moa.moa_server.domain.global.cursor.UpdatedAtVoteIdCursor;
import com.moa.moa_server.domain.global.cursor.VoteClosedCursor;
import com.moa.moa_server.domain.global.cursor.VotedAtVoteIdCursor;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.group.util.GroupLookupHelper;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.response.active.ActiveVoteItem;
import com.moa.moa_server.domain.vote.dto.response.active.ActiveVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.mine.MyVoteItem;
import com.moa.moa_server.domain.vote.dto.response.mine.MyVoteResponse;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteItem;
import com.moa.moa_server.domain.vote.dto.response.submitted.SubmittedVoteResponse;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.model.VoteWithVotedAt;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteListService {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int DEFAULT_UNAUTHENTICATED_PAGE_SIZE = 3;

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;

  private final GroupLookupHelper groupLookupHelper;
  private final VoteResultService voteResultService;
  private final CommentCountService commentCountService;

  @Transactional(readOnly = true)
  public ActiveVoteResponse getActiveVotes(
      @Nullable Long userId,
      @Nullable Long groupId,
      @Nullable String cursor,
      @Nullable Integer size) {
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    VoteClosedCursor parsedCursor = cursor != null ? VoteClosedCursor.parse(cursor) : null;

    // 비로그인 요청
    if (userId == null) {
      // 공개 그룹만 허용 (groupId가 1이 아닌 경우 거절)
      Long targetGroupId = (groupId != null) ? groupId : 1L;
      Group group =
          groupRepository
              .findById(targetGroupId)
              .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
      if (!group.isPublicGroup()) {
        throw new VoteException(VoteErrorCode.FORBIDDEN);
      }

      List<Vote> votes =
          voteRepository.findActiveVotes(
              List.of(group), parsedCursor, null, DEFAULT_UNAUTHENTICATED_PAGE_SIZE);
      List<ActiveVoteItem> items = votes.stream().map(ActiveVoteItem::from).toList();
      return new ActiveVoteResponse(items, null, false, items.size());
    }
    // 로그인 사용자 요청
    else {
      // 사용자 조회
      User user = getValidUser(userId);

      // 그룹 목록 수집 및 권한 검사
      List<Group> accessibleGroups = getAccessibleGroups(user, groupId);

      // 투표 목록 조회
      List<Vote> votes =
          voteRepository.findActiveVotes(accessibleGroups, parsedCursor, user, pageSize + 1);

      // 응답 구성
      boolean hasNext = votes.size() > pageSize;
      if (hasNext) votes = votes.subList(0, pageSize);

      String nextCursor =
          votes.isEmpty()
              ? null
              : new VoteClosedCursor(votes.getLast().getClosedAt(), votes.getLast().getCreatedAt())
                  .encode();

      List<ActiveVoteItem> items = votes.stream().map(ActiveVoteItem::from).toList();
      return new ActiveVoteResponse(items, nextCursor, hasNext, items.size());
    }
  }

  @Transactional
  public MyVoteResponse getMyVotes(
      Long userId, @Nullable Long groupId, @Nullable String cursor, @Nullable Integer size) {
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    UpdatedAtVoteIdCursor parsedCursor =
        cursor != null ? UpdatedAtVoteIdCursor.parse(cursor) : null;
    if (cursor != null && !voteRepository.existsById(parsedCursor.voteId())) {
      throw new VoteException(VoteErrorCode.VOTE_NOT_FOUND);
    }

    // 유저 조회 및 검증
    User user = getValidUser(userId);

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

      Group publicGroup = groupLookupHelper.getPublicGroup();
      if (!groups.contains(publicGroup)) {
        groups.add(publicGroup);
      }
    }

    // 사용자가 생성한 투표 목록 조회
    List<Vote> votes = voteRepository.findMyVotes(user, groups, parsedCursor, pageSize + 1);

    // 응답 구성
    boolean hasNext = votes.size() > pageSize;
    if (hasNext) votes = votes.subList(0, pageSize);

    String nextCursor =
        votes.isEmpty()
            ? null
            : new UpdatedAtVoteIdCursor(votes.getLast().getUpdatedAt(), votes.getLast().getId())
                .encode();

    // 댓글 수 집계하여 포함
    List<Long> voteIds = votes.stream().map(Vote::getId).toList();
    Map<Long, Integer> commentCounts = commentCountService.getCommentCountsByVoteIds(voteIds);

    // 각 투표별 집계 결과를 포함한 응답 DTO 구성
    List<MyVoteItem> items =
        votes.stream()
            .map(
                vote -> {
                  var results = voteResultService.getResultsWithVoteId(vote);
                  int commentsCount = commentCounts.getOrDefault(vote.getId(), 0);
                  return MyVoteItem.from(vote, results, commentsCount);
                })
            .toList();
    return new MyVoteResponse(items, nextCursor, hasNext, items.size());
  }

  @Transactional
  public SubmittedVoteResponse getSubmittedVotes(
      Long userId, @Nullable Long groupId, @Nullable String cursor, @Nullable Integer size) {

    int pageSize = resolvePageSize(size);
    VotedAtVoteIdCursor parsedCursor = cursor != null ? VotedAtVoteIdCursor.parse(cursor) : null;
    if (cursor != null && !voteRepository.existsById(parsedCursor.voteId())) {
      throw new VoteException(VoteErrorCode.VOTE_NOT_FOUND);
    }

    // 유저 조회 및 검증
    User user = getValidUser(userId);

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
      Group publicGroup = groupLookupHelper.getPublicGroup();
      if (!groups.contains(publicGroup)) {
        groups.add(publicGroup);
      }
    }

    // 참여한 투표 목록 조회
    List<VoteWithVotedAt> votes =
        voteRepository.findSubmittedVotes(user, groups, parsedCursor, pageSize + 1);

    // 응답 구성
    boolean hasNext = votes.size() > pageSize;
    if (hasNext) votes = votes.subList(0, pageSize);

    String nextCursor =
        votes.isEmpty()
            ? null
            : new VotedAtVoteIdCursor(votes.getLast().votedAt(), votes.getLast().vote().getId())
                .encode();

    // 댓글 수 집계하여 포함
    List<Long> voteIds = votes.stream().map(v -> v.vote().getId()).toList();
    Map<Long, Integer> commentCounts = commentCountService.getCommentCountsByVoteIds(voteIds);

    // 각 투표별 집계 결과를 포함한 응답 DTO 구성
    List<SubmittedVoteItem> items =
        votes.stream()
            .map(
                v -> {
                  var results = voteResultService.getResultsWithVoteId(v.vote());
                  int commentsCount = commentCounts.getOrDefault(v.vote().getId(), 0);
                  return SubmittedVoteItem.from(v.vote(), results, commentsCount);
                })
            .toList();
    return new SubmittedVoteResponse(items, nextCursor, hasNext, items.size());
  }

  private User getValidUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  private int resolvePageSize(@Nullable Integer size) {
    return (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
  }

  /**
   * 접근 가능한 그룹 목록 조회 (투표 리스트 조회에 사용) - groupId가 지정된 경우: 해당 그룹만 조회 (권한 확인 포함) - groupId가 없는 경우: 공개 그룹
   * + 사용자가 속한 모든 그룹 반환
   */
  private List<Group> getAccessibleGroups(User user, @Nullable Long groupId) {
    if (groupId != null) {
      // 단일 그룹만 조회 (권한 확인 포함)
      Group group =
          groupRepository
              .findById(groupId)
              .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));

      if (!group.isPublicGroup()) {
        groupMemberRepository
            .findByGroupAndUser(group, user)
            .orElseThrow(() -> new VoteException(VoteErrorCode.FORBIDDEN));
      }

      return List.of(group);
    }

    // 전체 그룹 조회: 공개 그룹 + 사용자의 그룹
    Group publicGroup = groupLookupHelper.getPublicGroup();
    List<Group> userGroups = groupMemberRepository.findAllActiveGroupsByUser(user);

    return Stream.concat(Stream.of(publicGroup), userGroups.stream()).distinct().toList();
  }
}
