package com.moa.moa_server.domain.ranking.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.handler.GroupErrorCode;
import com.moa.moa_server.domain.group.handler.GroupException;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.ranking.dto.TopVoteItem;
import com.moa.moa_server.domain.ranking.dto.TopVoteResponse;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.user.handler.UserErrorCode;
import com.moa.moa_server.domain.user.handler.UserException;
import com.moa.moa_server.domain.user.repository.UserRepository;
import com.moa.moa_server.domain.user.util.AuthUserValidator;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.service.vote_result.VoteResultService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final StringRedisTemplate redisTemplate;
  private final VoteRepository voteRepository;
  private final VoteResultService voteResultService;
  private final UserRepository userRepository;
  private final GroupRepository groupRepository;
  private final GroupMemberRepository groupMemberRepository;

  @Transactional
  public TopVoteResponse getTopVotes(Long userId, Long groupId) {
    // 유저/그룹/멤버십 검증
    User user = validateAndGetuser(userId);
    validateGroupMembership(user, groupId);

    // 랭킹 기준 시간 계산
    LocalDate date = LocalDate.now(ZoneOffset.UTC);
    LocalDateTime from = date.atStartOfDay();
    LocalDateTime to = date.atTime(LocalTime.MAX);

    List<TopVoteItem> topItems;

    if (groupId != null) {
      topItems = getTopVotesByGroup(groupId);
    } else {
      topItems = getTopVotesByAllGroups(user);
    }

    return TopVoteResponse.of(groupId, from, to, topItems);
  }

  private User validateAndGetuser(Long userId) {
    // 유저 조회 및 활성 상태 확인
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    AuthUserValidator.validateActive(user);
    return user;
  }

  private void validateGroupMembership(User user, Long groupId) {
    // 그룹 ID가 없으면 전체 그룹 조회이므로 검증 생략
    if (groupId == null) return;

    // 그룹 조회
    Group group =
        groupRepository
            .findById(groupId)
            .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_NOT_FOUND));

    // 비공개 그룹이라면 멤버십 확인
    if (!group.isPublicGroup()) {
      groupMemberRepository
          .findByGroupAndUser(group, user)
          .orElseThrow(() -> new GroupException(GroupErrorCode.FORBIDDEN));
    }
  }

  private List<TopVoteItem> getTopVotesByGroup(Long groupId) {
    String key = buildRankingKey(groupId);

    // top3 조회
    Set<String> voteIds = redisTemplate.opsForZSet().reverseRange(key, 0, 2);

    // top3 데이터가 없으면 빈 리스트 반환
    if (voteIds == null || voteIds.isEmpty()) {
      return Collections.emptyList();
    }

    // voteId 리스트를 기반으로 투표 정보 조회 후 TopVoteItem DTO로 변환
    List<Long> idList = voteIds.stream().map(Long::valueOf).collect(Collectors.toList());
    List<Vote> votes = voteRepository.findAllById(idList);

    return votes.stream().map(this::toTopVoteItem).collect(Collectors.toList());
  }

  private List<TopVoteItem> getTopVotesByAllGroups(User user) {
    List<Long> groupIds = groupMemberRepository.findGroupIdsByUser(user);
    groupIds.add(1L); // 공개 그룹 포함

    return groupIds.stream()
        .flatMap(gid -> getTopVotesWithScore(gid).stream())
        .sorted(Map.Entry.<TopVoteItem, Double>comparingByValue().reversed())
        .limit(3)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
  }

  private String buildRankingKey(Long groupId) {
    String base = "ranking";
    String date = LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
    String g = (groupId != null) ? String.valueOf(groupId) : "all";
    return base + ":" + g + ":" + date; // 예: ranking:3:20250716
  }

  private List<Map.Entry<TopVoteItem, Double>> getTopVotesWithScore(Long groupId) {
    String key = buildRankingKey(groupId);
    Set<ZSetOperations.TypedTuple<String>> tuples =
        redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 2);

    if (tuples == null || tuples.isEmpty()) {
      return Collections.emptyList();
    }

    List<Long> voteIds =
        tuples.stream()
            .map(t -> Long.valueOf(Objects.requireNonNull(t.getValue())))
            .collect(Collectors.toList());

    Map<Long, Double> scoreMap =
        tuples.stream()
            .filter(t -> t.getScore() != null && t.getValue() != null)
            .collect(
                Collectors.toMap(
                    t -> Long.valueOf(t.getValue()), // null 아님 보장됨
                    ZSetOperations.TypedTuple::getScore));

    List<Vote> votes = voteRepository.findAllById(voteIds);

    return votes.stream()
        .map(
            vote -> {
              List<VoteOptionResult> results = voteResultService.getResults(vote);
              TopVoteItem topVoteItem = TopVoteItem.from(vote, results);
              Double score = scoreMap.get(vote.getId());
              return Map.entry(topVoteItem, score);
            })
        .collect(Collectors.toList());
  }

  private TopVoteItem toTopVoteItem(Vote vote) {
    List<VoteOptionResult> results = voteResultService.getResults(vote);
    return TopVoteItem.from(vote, results);
  }
}
