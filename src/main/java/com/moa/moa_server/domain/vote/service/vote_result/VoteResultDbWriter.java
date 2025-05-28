package com.moa.moa_server.domain.vote.service.vote_result;

import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.entity.VoteResult;
import com.moa.moa_server.domain.vote.model.ResultRaw;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import com.moa.moa_server.domain.vote.repository.VoteResultRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 종료된 투표에 대해 결과를 집계하고 DB에 저장하는 서비스 */
@Service
@RequiredArgsConstructor
public class VoteResultDbWriter {

  private final VoteResponseRepository voteResponseRepository;
  private final VoteResultRepository voteResultRepository;
  private final VoteRepository voteRepository;

  /** 종료된 투표에 대해 투표 결과 집계 및 DB 저장 후 반환 */
  public List<ResultRaw> finalize(Vote vote) {
    // 투표 상태 변경
    vote.close();
    voteRepository.save(vote);

    // 투표 결과 집계
    List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);
    Map<Integer, Long> countMap =
        responses.stream()
            .filter(vr -> vr.getOptionNumber() > 0)
            .collect(Collectors.groupingBy(VoteResponse::getOptionNumber, Collectors.counting()));

    int totalCount = countMap.values().stream().mapToInt(Number::intValue).sum();

    // vote_result 테이블에 집계 결과 저장
    List<VoteResult> results =
        Stream.of(1, 2)
            .map(
                option -> {
                  int count = countMap.getOrDefault(option, 0L).intValue();
                  double ratio = totalCount == 0 ? 0.0 : (count * 100.0) / totalCount;

                  return VoteResult.create(vote, option, count, ratio);
                })
            .toList();

    voteResultRepository.saveAll(results);

    // ResultRaw로 변환 후 반환
    return results.stream()
        .map(r -> new ResultRaw(r.getOptionNumber(), r.getCount(), r.getRatio().doubleValue()))
        .toList();
  }
}
