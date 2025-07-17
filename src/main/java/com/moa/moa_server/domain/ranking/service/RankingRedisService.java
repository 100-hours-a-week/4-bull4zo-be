package com.moa.moa_server.domain.ranking.service;

import com.moa.moa_server.domain.vote.entity.Vote;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingRedisService {

  private final StringRedisTemplate redisTemplate;

  public void trackUpdatedVote(Long voteId) {
    String key =
        "ranking:changed:" + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);
    long now = Instant.now().getEpochSecond(); // score: 현재 timestamp
    redisTemplate.opsForZSet().add(key, voteId.toString(), now);
  }

  public boolean isTopRankedVote(Vote vote) {
    String groupKey =
        vote.getGroup().isPublicGroup() ? "1" : String.valueOf(vote.getGroup().getId());
    String key =
        "ranking:"
            + groupKey
            + ":"
            + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.BASIC_ISO_DATE);

    // Redis ZSet에서 Top3 범위 조회
    Set<String> top3 = redisTemplate.opsForZSet().reverseRange(key, 0, 2);
    if (top3 == null) return false;

    return top3.contains(String.valueOf(vote.getId()));
  }
}
