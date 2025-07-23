package com.moa.moa_server.domain.notification.application.producer;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.notification.application.event.EventPublisher;
import com.moa.moa_server.domain.notification.application.event.NotificationEvent;
import com.moa.moa_server.domain.notification.entity.NotificationType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupNotificationProducerImpl implements NotificationProducer {

  private final GroupMemberRepository groupMemberRepository;
  private final EventPublisher eventPublisher;

  public void notifyAllMembersGroupDeleted(Group group) {
    Long ownerId = group.getUser().getId();

    List<Long> memberIds = groupMemberRepository.findUserIdsByGroupExcludingOwner(group, ownerId);

    String content = group.getName() + " 그룹이 삭제되었습니다.";
    NotificationEvent event =
        NotificationEvent.forMultipleUsers(
            memberIds, NotificationType.GROUP_DELETED, content, null);
    eventPublisher.publish(event);
  }
}
