package com.moa.moa_server.domain.notification.producer;

import com.moa.moa_server.domain.notification.entity.NotificationType;
import com.moa.moa_server.domain.notification.event.EventPublisher;
import com.moa.moa_server.domain.notification.event.NotificationEvent;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteNotificationProducerImpl implements NotificationProducer {

  private final VoteRepository voteRepository;
  private final EventPublisher eventPublisher;

  public void notifyVoteCommented(Long voteId, Long commenterId) {
    Vote vote =
        voteRepository
            .findById(voteId)
            .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
    Long authorId = vote.getUser().getId();

    // 본인이 자기 투표에 댓글 단 경우는 알림 안 보냄
    if (authorId.equals(commenterId)) {
      return;
    }

    String content = "내 투표에 댓글이 달렸습니다.";
    String url = "https://moagenda.com/research/" + voteId;

    // 이벤트 발행
    NotificationEvent event =
        NotificationEvent.forSingleUser(authorId, NotificationType.MY_VOTE_COMMENT, content, url);
    eventPublisher.publish(event);
  }
}
