package com.moa.moa_server.domain.ranking.scheduler;

import com.moa.moa_server.domain.comment.repository.CommentRepository;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

  private final StringRedisTemplate redisTemplate;
  private final VoteRepository voteRepository;
  private final CommentRepository commentRepository;
  private final VoteResponseRepository voteResponseRepository;

  @Scheduled(cron = "0 * * * * *") // 매 정시
  public void updateVoteRanking() {
    // 1. 최근 1시간 내로 수정된 투표 가져오기
    Set<String> updatedVoteIds = getRecentlyModifiedVotes();

    for (String voteIdStr : updatedVoteIds) {
      Long voteId = Long.parseLong(voteIdStr);
      Vote vote = voteRepository.findById(voteId).orElse(null);
      if (vote == null) continue;

      // 2. 점수 계산
      int commentCount = commentRepository.countByVoteId(voteId);
      int responseCount = voteResponseRepository.countByVoteId(voteId);
      long duration = computeDuration(vote.getOpenAt(), vote.getClosedAt());
      double score = (commentCount + responseCount) / (double) duration;

      // 3. 랭킹 ZSet에 저장
      String key =
          "ranking:"
              + vote.getGroup().getId()
              + ":"
              + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
      redisTemplate.opsForZSet().add(key, voteIdStr, score);
    }

    log.info(
        "[RankingScheduler#updateVoteRanking] 랭킹 점수 스케줄러 완료 - updatedVotes={}개",
        updatedVoteIds.size());
  }

  private Set<String> getRecentlyModifiedVotes() {
    String key =
        "ranking:changed:" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
    long now = Instant.now().getEpochSecond();

    Set<String> raw = redisTemplate.opsForZSet().rangeByScore(key, now - 3600, now);

    if (raw == null || raw.isEmpty()) {
      return Collections.emptySet();
    }

    return raw.stream().map(Object::toString).collect(Collectors.toSet());
  }

  private long computeDuration(LocalDateTime openAt, LocalDateTime closedAt) {
    long hours = Duration.between(openAt, closedAt).toHours();
    return Math.max(1, (long) Math.ceil(hours / 24.0));
  }
}
