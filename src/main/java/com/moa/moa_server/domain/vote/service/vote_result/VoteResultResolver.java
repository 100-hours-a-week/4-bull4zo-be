package com.moa.moa_server.domain.vote.service.vote_result;

import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.entity.VoteResult;
import com.moa.moa_server.domain.vote.model.ResultRaw;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.repository.VoteResultRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 투표 상태에 따라 결과 조회 경로(DB, Redis)를 결정하는 중간 조정 서비스 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoteResultResolver {

  private final VoteResponseRepository voteResponseRepository;
  private final VoteResultRepository voteResultRepository;

  private final VoteResultRedisService voteResultRedisService;
  private final VoteResultDbWriter voteResultDbWriter;

  /** 캐시 또는 DB 상태에 따라 투표 결과를 조회하거나 계산 */
  public List<ResultRaw> getOrComputeResults(Vote vote) {
    // 1. 종료된 투표: DB에서 조회 또는 종료 처리
    log.debug(
        "voteClosedAt={}, now={}, result={}",
        vote.getClosedAt(),
        LocalDateTime.now(),
        vote.getClosedAt().isBefore(LocalDateTime.now()));
    if (vote.getClosedAt().isBefore(LocalDateTime.now())) {
      if (voteResultRepository.existsByVoteId(vote.getId())) {
        return getResultsFromDbOnly(vote); // 이미 집계된 결과 조회
      }
      return voteResultDbWriter.finalize(vote); // 최초 종료 처리
    }

    // 2. 진행 중 투표: Redis 캐시 조회
    Map<Integer, Integer> cached = voteResultRedisService.getOptionCounts(vote.getId());
    if (cached.containsKey(1) && cached.containsKey(2)) {
      return toResultRawListInt(cached);
    }

    // 3. Redis miss → 실시간 집계 후 Redis에 저장
    log.warn(
        "[Redis MISS] voteId={} - 예상치 못한 Redis 캐시 유실 또는 DB 저장 실패 가능성. 실시간으로 집계 후 Redis에 저장합니다.",
        vote.getId());
    return computeAndCacheResults(vote);
  }

  /** vote_result 테이블에서 결과 조회 */
  private List<ResultRaw> getResultsFromDbOnly(Vote vote) {
    List<VoteResult> results = voteResultRepository.findAllByVoteId(vote.getId());
    return results.stream()
        .map(r -> new ResultRaw(r.getOptionNumber(), r.getCount(), r.getRatio().doubleValue()))
        .toList();
  }

  /** 실시간 집계 후 Redis에 저장, 결과 계산하여 반환 */
  private List<ResultRaw> computeAndCacheResults(Vote vote) {
    List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);

    Map<Integer, Long> countMap =
        responses.stream()
            .filter(vr -> vr.getOptionNumber() > 0)
            .collect(Collectors.groupingBy(VoteResponse::getOptionNumber, Collectors.counting()));

    Map<Integer, Integer> intMap =
        countMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

    // Redis에 저장
    voteResultRedisService.setCountsWithTTL(vote.getId(), intMap, vote.getClosedAt());

    return toResultRawListLong(countMap);
  }

  /** countMap(Long) 기반으로 ResultRaw 리스트 생성 */
  private List<ResultRaw> toResultRawListLong(Map<Integer, Long> countMap) {
    int total = countMap.values().stream().mapToInt(Number::intValue).sum();
    return Stream.of(1, 2)
        .map(
            option -> {
              int count = countMap.getOrDefault(option, 0L).intValue();
              double ratio = total == 0 ? 0.0 : (count * 100.0) / total;
              return new ResultRaw(option, count, ratio);
            })
        .toList();
  }

  /** countMap(Integer) 기반으로 ResultRaw 리스트 생성 */
  private List<ResultRaw> toResultRawListInt(Map<Integer, Integer> countMap) {
    int total = countMap.values().stream().mapToInt(Number::intValue).sum();
    return Stream.of(1, 2)
        .map(
            option -> {
              int count = countMap.getOrDefault(option, 0);
              double ratio = total == 0 ? 0.0 : (count * 100.0) / total;
              return new ResultRaw(option, count, ratio);
            })
        .toList();
  }
}
