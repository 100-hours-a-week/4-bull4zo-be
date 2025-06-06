package com.moa.moa_server.domain.vote.service.vote_result;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** 사용자에게 제공될 투표 결과 응답을 구성하는 최상위 서비스 */
@Service
@RequiredArgsConstructor
public class VoteResultService {

  private final VoteResultResolver voteResultResolver;

  public List<VoteOptionResult> getResults(Vote vote) {
    return voteResultResolver.getOrComputeResults(vote).stream()
        .map(r -> new VoteOptionResult(r.optionNumber(), r.count(), r.ratio()))
        .toList();
  }

  public List<VoteOptionResultWithId> getResultsWithVoteId(Vote vote) {
    return voteResultResolver.getOrComputeResults(vote).stream()
        .map(r -> new VoteOptionResultWithId(vote.getId(), r.optionNumber(), r.count(), r.ratio()))
        .toList();
  }
}
