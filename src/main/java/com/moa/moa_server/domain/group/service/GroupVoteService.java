package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.global.cursor.CreatedAtVoteIdCursor;
import com.moa.moa_server.domain.group.dto.group_vote.GroupVoteItem;
import com.moa.moa_server.domain.group.dto.group_vote.GroupVoteListResponse;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupVoteService {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;
  private final VoteRepository voteRepository;

  private final VoteResultService voteResultService;

  /** 그룹 투표 목록 조회 */
  @Transactional(readOnly = true)
  public GroupVoteListResponse getGroupVotes(
      Long userId, Long groupId, @Nullable String cursor, @Nullable Integer size) {
    // 그룹, 사용자 검증, 권한 검사
    User user = findActiveUser(userId);
    Group group = findGroup(groupId);
    GroupMember member =
        groupMemberRepository
            .findByGroupAndUser(group, user)
            .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));
    if (!member.isOwnerOrManager())
      throw new GroupException(GroupErrorCode.FORBIDDEN); // 그룹 소유자, 관리자만 가능

    // 커서 파싱
    int pageSize = (size == null || size <= 0) ? DEFAULT_PAGE_SIZE : size;
    CreatedAtVoteIdCursor parsedCursor =
        (cursor != null) ? CreatedAtVoteIdCursor.parse(cursor) : null;

    // 목록 조회
    List<Vote> votes = voteRepository.findVotesInGroup(group, parsedCursor, pageSize + 1);

    // 응답 구성
    boolean hasNext = votes.size() > pageSize;
    if (hasNext) votes = votes.subList(0, pageSize);

    String nextCursor =
        votes.isEmpty()
            ? null
            : new CreatedAtVoteIdCursor(votes.getLast().getCreatedAt(), votes.getLast().getId())
                .encode();

    // 각 투표별 집계 포함
    List<GroupVoteItem> voteItems =
        votes.stream()
            .map(
                vote ->
                    new GroupVoteItem(
                        vote.getId(),
                        vote.getContent(),
                        vote.getCreatedAt(),
                        vote.getClosedAt(),
                        voteResultService.getResults(vote)))
            .toList();

    // 응답 변환
    return new GroupVoteListResponse(
        group.getId(), group.getName(), voteItems, nextCursor, hasNext, voteItems.size());
  }

  private User findActiveUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  private Group findGroup(Long groupId) {
    return groupRepository
        .findById(groupId)
        .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));
  }
}
