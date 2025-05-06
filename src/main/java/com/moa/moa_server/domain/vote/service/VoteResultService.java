package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.vote.dto.response.VoteOptionResultWithId;
import com.moa.moa_server.domain.vote.dto.response.result.VoteOptionResult;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import com.moa.moa_server.domain.vote.repository.VoteResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class VoteResultService {

    private final VoteResponseRepository voteResponseRepository;

    /**
     * 투표 옵션 결과만 필요한 경우 (optionNumber, count, ratio)
     */
    public List<VoteOptionResult> getResults(Vote vote) {
        return getResultsInternal(vote).stream()
                .map(r -> new VoteOptionResult(r.optionNumber(), r.count(), r.ratio()))
                .toList();
    }

    /**
     * voteId까지 포함된 결과가 필요한 경우 (MyVote용)
     */
    public List<VoteOptionResultWithId> getResultsWithVoteId(Vote vote) {
        return getResultsInternal(vote).stream()
                .map(r -> new VoteOptionResultWithId(vote.getId(), r.optionNumber(), r.count(), r.ratio()))
                .toList();
    }

    private List<ResultRaw> getResultsInternal(Vote vote) {
        List<VoteResponse> responses = voteResponseRepository.findAllByVote(vote);

        int totalCount = (int) responses.stream()
                .filter(vr -> vr.getOptionNumber() > 0)
                .count();

        Map<Integer, Long> countMap = responses.stream()
                .filter(vr -> vr.getOptionNumber() > 0)
                .collect(Collectors.groupingBy(
                        VoteResponse::getOptionNumber,
                        Collectors.counting()
                ));

        return Stream.of(1,2)
                .map(option -> {
                    int count = countMap.getOrDefault(option, 0L).intValue();
                    int ratio = totalCount == 0 ? 0 : (int) ((count * 100.0) / totalCount);
                    return new ResultRaw(option, count, ratio);
                })
                .toList();
    }

    private record ResultRaw(int optionNumber, int count, int ratio) {}

}