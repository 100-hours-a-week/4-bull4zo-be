package com.moa.moa_server.domain.vote.service.vote_result;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/** Redis에서 투표 결과를 조회하거나 캐시하는 서비스 */
@Service
@RequiredArgsConstructor
public class VoteResultRedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PREFIX = "vote_result:";

  public void incrementOptionCount(Long voteId, int optionNumber) {
    String key = PREFIX + voteId;
    redisTemplate.opsForHash().increment(key, String.valueOf(optionNumber), 1);
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

  public Map<Integer, Integer> getOptionCounts(Long voteId) {
    String key = PREFIX + voteId;
    Map<Object, Object> raw = redisTemplate.opsForHash().entries(key);

    Map<Integer, Integer> result = new HashMap<>();
    for (Map.Entry<Object, Object> entry : raw.entrySet()) {
      Integer keyInt = Integer.parseInt(entry.getKey().toString());
      Integer valueInt = Integer.parseInt(entry.getValue().toString());
      result.put(keyInt, valueInt);
    }
    return result;
  }
}
