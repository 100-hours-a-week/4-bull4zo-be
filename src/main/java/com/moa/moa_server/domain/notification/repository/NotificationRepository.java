package com.moa.moa_server.domain.notification.repository;

import com.moa.moa_server.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository
    extends JpaRepository<Notification, Long>, NotificationRepositoryCustom {

  List<Notification> findByUserIdAndIdGreaterThanOrderById(Long userId, Long id);
}
