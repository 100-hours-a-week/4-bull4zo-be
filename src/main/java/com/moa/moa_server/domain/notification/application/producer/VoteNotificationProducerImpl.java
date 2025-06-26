package com.moa.moa_server.domain.notification.application.producer;

import com.moa.moa_server.domain.notification.application.event.EventPublisher;
import com.moa.moa_server.domain.notification.application.event.NotificationEvent;
import com.moa.moa_server.domain.notification.entity.NotificationType;
import com.moa.moa_server.domain.notification.util.NotificationContentFormatter;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import com.moa.moa_server.domain.vote.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoteNotificationProducerImpl implements NotificationProducer {

  private final VoteRepository voteRepository;
  private final EventPublisher eventPublisher;

  @Value("${frontend.url}")
  private String frontendUrl;

  public void notifyVoteCommented(Long voteId, Long commenterId, String commentContent) {
    sendSingleVoteNotification(
        voteId, NotificationType.MY_VOTE_COMMENT, commentContent, commenterId);
  }

  public void notifyVoteApproved(Vote vote) {
    sendSingleVoteNotification(
        vote.getId(), NotificationType.VOTE_APPROVED, vote.getContent(), null);
  }

  public void notifyVoteRejected(Vote vote) {
    sendSingleVoteNotification(
        vote.getId(), NotificationType.VOTE_REJECTED, vote.getContent(), null);
  }

  private void sendSingleVoteNotification(
      Long voteId, NotificationType type, String content, Long excludeUserId) {
    Vote vote = getVoteOrThrow(voteId);
    Long authorId = vote.getUser().getId();

    if (authorId.equals(excludeUserId)) return;

    String truncated = NotificationContentFormatter.truncateContent(content); // 알림 내용
    String url = getVoteUrl(voteId); // 알림 URL

    NotificationEvent event = NotificationEvent.forSingleUser(authorId, type, truncated, url);
    eventPublisher.publish(event);
  }

  private Vote getVoteOrThrow(Long voteId) {
    return voteRepository
        .findById(voteId)
        .orElseThrow(() -> new VoteException(VoteErrorCode.VOTE_NOT_FOUND));
  }

  private String getVoteUrl(Long voteId) {
    return String.format("%s/research/%d", frontendUrl, voteId);
  }
}
