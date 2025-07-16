package com.moa.moa_server.domain.ranking.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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
}
