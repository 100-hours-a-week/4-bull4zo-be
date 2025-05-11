package com.moa.moa_server.domain.feedback.repository;

import com.moa.moa_server.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
