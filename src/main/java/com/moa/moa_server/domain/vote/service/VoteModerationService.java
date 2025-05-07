package com.moa.moa_server.domain.vote.service;

import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackRequest;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationCallbackResponse;
import com.moa.moa_server.domain.vote.dto.moderation.VoteModerationRequest;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteModerationLog;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteModerationLogRepository;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteModerationService {

    @Value("${ai.server-url}")
    private String aiServerUrl;

    private final VoteRepository voteRepository;
    private final VoteModerationLogRepository moderationLogRepository;

    private final RestTemplate restTemplate;

    public void requestModeration(Long voteId, String content) {
        String moderationUrl = aiServerUrl + "/api/v1/moderation";

        VoteModerationRequest request = new VoteModerationRequest(voteId, content);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    moderationUrl,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("[VoteModerationService#requestModeration] Moderation 요청 실패: status: {}, body: {}", response.getStatusCode(), response.getBody());
            } else {
                log.info("[VoteModerationService#requestModeration] Moderation 요청 성공: voteId: {}", voteId);
            }

        } catch (Exception e) {
            log.error("[VoteModerationService#requestModeration] Moderation 요청 중 예외 발생: voteId: {}, error: {}", voteId, e.getMessage(), e);
        }
    }

    @Transactional
    public VoteModerationCallbackResponse handleCallback(VoteModerationCallbackRequest request) {
//        Vote vote = voteRepository.findById(request.voteId())
//                .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
//
//        // result enum 매핑
//        Vote.VoteStatus status = Vote.VoteStatus.valueOf(request.result().toUpperCase());
//        ModerationRejectReason reason = ModerationRejectReason.valueOf(request.reason().toUpperCase());
//
//        // 상태 반영
//        vote.updateModerationResult(status);
//
//        // 로그 저장
//        VoteModerationLog log = VoteModerationLog.of(
//                vote,
//                status,
//                reason,
//                request.reasonDetail(),
//                request.version()
//        );
//        moderationLogRepository.save(log);
//
//        return new VoteModerationResult(vote.getId(), true);
        return null;
    }
}
