package com.moa.moa_server.domain.vote.service.v3;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteResultRedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PREFIX = "vote_result:";

  public void incrementOptionCount(Long voteId, int optionNumber) {
    String key = PREFIX + voteId;
    redisTemplate.opsForHash().increment(key, String.valueOf(optionNumber), 1);
  }

  public void setOptionCount(Long voteId, int optionNumber, int count) {
    String key = PREFIX + voteId;
    redisTemplate.opsForHash().put(key, String.valueOf(optionNumber), count);
  }

  public void setCountsWithTTL(Long voteId, Map<Integer, Integer> counts, LocalDateTime closedAt) {
    String key = PREFIX + voteId;

    counts.forEach(
        (option, count) -> redisTemplate.opsForHash().put(key, String.valueOf(option), count));

    Duration ttl = Duration.between(LocalDateTime.now(), closedAt.plusHours(6));
    if (!ttl.isNegative() && !ttl.isZero()) {
      redisTemplate.expire(key, ttl);
    }
  }

  public Map<String, Integer> getOptionCounts(Long voteId) {
    String key = PREFIX + voteId;
    Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);
    Map<String, Integer> result = new HashMap<>();
    for (Object option : raw.keySet()) {
      result.put(option.toString(), Integer.parseInt(raw.get(option).toString()));
    }
    return result;
  }

  public void initializeCounts(Long voteId) {
    String key = PREFIX + voteId;
    redisTemplate.opsForHash().put(key, "1", 0);
    redisTemplate.opsForHash().put(key, "2", 0);
  }

  public void deleteResult(Long voteId) {
    redisTemplate.delete(PREFIX + voteId);
  }
}
