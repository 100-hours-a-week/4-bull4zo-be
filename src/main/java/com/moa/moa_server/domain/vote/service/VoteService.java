package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.vote.dto.request.VoteCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoteService {

    public Long createVote(Long userId, VoteCreateRequest request) {
        return null;
    }
}
