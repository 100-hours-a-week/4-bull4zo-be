package com.moa.moa_server.domain.ranking.scheduler;

import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.ranking.entity.VoteRanking;
import com.moa.moa_server.domain.ranking.repository.VoteRankingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoteRankingWriteScheduler {

  private final StringRedisTemplate redisTemplate;
  private final VoteRankingRepository voteRankingRepository;
  private final GroupRepository groupRepository;

  @Transactional
  @Scheduled(cron = "0 0 * * * *") // 매 정시
  public void persistVoteRankings() {
    LocalDateTime rankedAt = LocalDateTime.now(ZoneOffset.UTC); // 저장 시각
    String dateKey =
        LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE); // Redis 키 날짜
    List<VoteRanking> rankingsToSave = collectTopRankings(rankedAt, dateKey);

    try {
      voteRankingRepository.saveAll(rankingsToSave);
      log.info("[VoteRankingWriteScheduler] 저장 완료 - 총 {}건", rankingsToSave.size());
    } catch (Exception e) {
      log.error(
          "[VoteRankingWriteScheduler] 저장 실패 - 저장 대상 {}건, reason={}",
          rankingsToSave.size(),
          e.getMessage(),
          e);
    }
  }

  /** 모든 그룹에 대해 Redis에서 Top3 랭킹을 조회하고, DB 저장용 VoteRanking 리스트를 생성. */
  private List<VoteRanking> collectTopRankings(LocalDateTime rankedAt, String dateKey) {
    List<Long> groupIds = groupRepository.findAllGroupIds();
    List<VoteRanking> result = new ArrayList<>();

    log.info("[VoteRankingWriteScheduler] 시작 - {}개 그룹 대상 랭킹 기록", groupIds.size());

    // 각 그룹에 대해 Top3 랭킹 데이터 조회해 VoteRanking 객체로 반환
    for (Long groupId : groupIds) {
      result.addAll(buildRankingsFromRedis(groupId, rankedAt, dateKey));
    }

    return result;
  }

  /** Redis에서 특정 그룹의 Top3 투표를 조회하여, DB 저장용 VoteRanking 리스트로 변환. */
  private List<VoteRanking> buildRankingsFromRedis(
      Long groupId, LocalDateTime rankedAt, String dateKey) {
    // Redis key 생성: ranking:{groupId}:{yyyyMMdd}
    String key = "ranking:" + groupId + ":" + dateKey;

    // 해당 그룹의 Redis Sorted Set에서 Top3 투표 ID 조회
    Set<ZSetOperations.TypedTuple<String>> topVotes =
        redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 2);

    if (topVotes == null || topVotes.isEmpty()) return Collections.emptyList();

    List<VoteRanking> rankings = new ArrayList<>();
    int rank = 1;

    for (ZSetOperations.TypedTuple<String> tuple : topVotes) {
      String voteIdStr = tuple.getValue();
      if (voteIdStr == null) {
        log.warn("[VoteRankingWriteScheduler] 랭킹 키={}에 null인 voteId가 포함되어 있어 건너뜁니다.", key);
        continue;
      }

      VoteRanking ranking = VoteRanking.of(Long.parseLong(voteIdStr), groupId, rank++, rankedAt);
      rankings.add(ranking);
    }

    return rankings;
  }
}
