package com.moa.moa_server.domain.vote.service.v3;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoteResultServiceV3 {

  private final VoteResponseRepository voteResponseRepository;
  private final VoteResultRedisService voteResultRedisService;

  public List<VoteOptionResult> getResults(Vote vote) {
    return getOrComputeResults(vote).stream()
        .map(r -> new VoteOptionResult(r.optionNumber(), r.count(), r.ratio()))
        .toList();
  }

  public List<VoteOptionResultWithId> getResultsWithVoteId(Vote vote) {
    return getOrComputeResults(vote).stream()
        .map(r -> new VoteOptionResultWithId(vote.getId(), r.optionNumber(), r.count(), r.ratio()))
        .toList();
  }

  private List<ResultRaw> getOrComputeResults(Vote vote) {
    // Redis에서 캐시 조회 후 결과 생성
    Map<String, Integer> cached = voteResultRedisService.getOptionCounts(vote.getId());
    if (cached != null && cached.size() == 2) {
      log.debug(
          "[VoteResultServiceV3#getOrComputeResults] Redis cache hit for voteId={}", vote.getId());

      int total = cached.values().stream().mapToInt(Integer::intValue).sum();
      return Stream.of(1, 2)
          .map(
              option -> {
                int count = cached.getOrDefault(String.valueOf(option), 0);
                double ratio = total == 0 ? 0.0 : (count * 100.0) / total;
                return new ResultRaw(option, count, ratio);
              })
          .toList();
    }

    log.debug(
        "[VoteResultServiceV3#getOrComputeResults] Redis cache miss for voteId={}, fallback to DB",
        vote.getId());

    // fallback to DB (실시간 집계)
    List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);
    int totalCount = (int) responses.stream().filter(vr -> vr.getOptionNumber() > 0).count();

    Map<Integer, Long> countMap =
        responses.stream()
            .filter(vr -> vr.getOptionNumber() > 0)
            .collect(Collectors.groupingBy(VoteResponse::getOptionNumber, Collectors.counting()));

    Map<Integer, Integer> intMap =
        countMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().intValue()));

    // Redis에 저장
    voteResultRedisService.setCountsWithTTL(vote.getId(), intMap, vote.getClosedAt());

    return Stream.of(1, 2)
        .map(
            option -> {
              int count = countMap.getOrDefault(option, 0L).intValue();
              double ratio = totalCount == 0 ? 0.0 : (count * 100.0) / totalCount;
              return new ResultRaw(option, count, ratio);
            })
        .toList();
  }

  private record ResultRaw(int optionNumber, int count, double ratio) {}
}
