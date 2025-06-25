package com.moa.moa_server.domain.notification.producer;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.notification.entity.NotificationType;
import com.moa.moa_server.domain.notification.event.EventPublisher;
import com.moa.moa_server.domain.notification.event.NotificationEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupNotificationProducerImpl implements NotificationProducer {

  private final GroupMemberRepository groupMemberRepository;
  private final EventPublisher eventPublisher;

  public void notifyAllMembersGroupDeleted(Group group) {

    List<Long> members =
        groupMemberRepository.findAllByGroup(group).stream()
            .map(GroupMember -> GroupMember.getUser().getId())
            .toList();

    String content = group.getName() + " 그룹이 삭제되었습니다.";
    NotificationEvent event =
        NotificationEvent.forMultipleUsers(members, NotificationType.GROUP_DELETED, content, null);
    eventPublisher.publish(event);
  }
}
