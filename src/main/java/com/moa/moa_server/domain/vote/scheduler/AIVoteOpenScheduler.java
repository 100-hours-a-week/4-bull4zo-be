package com.moa.moa_server.domain.vote.scheduler;

import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** AI 생성 투표 중 PENDING 상태이고, openAt 시간이 지난 투표를 OPEN 상태로 변경하는 스케줄러. 매일 오전 10시 5분(KST)에 실행. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AIVoteOpenScheduler {

  private final VoteRepository voteRepository;

  @Scheduled(cron = "0 5 10 * * *", zone = "Asia/Seoul") // 초 분 시 일 월 요일
  @Transactional
  public void openAIVotes() {
    LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

    List<Vote> pendingVotes =
        voteRepository.findAllByVoteStatusAndVoteTypeAndOpenAtLessThanEqual(
            Vote.VoteStatus.PENDING, Vote.VoteType.AI, now);

    if (pendingVotes.isEmpty()) return;

    for (Vote vote : pendingVotes) {
      vote.open();
    }

    log.info("AI 투표 자동 오픈 처리 완료: {}건", pendingVotes.size());
  }
}
