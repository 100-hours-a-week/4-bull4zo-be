package com.moa.moa_server.domain.groupanalysis.repository;

import com.moa.moa_server.domain.groupanalysis.entity.GroupAnalysis;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupAnalysisJpaRepository extends JpaRepository<GroupAnalysis, Long> {
  boolean existsByGroupIdAndWeekStartAt(Long groupId, LocalDateTime weekStartAt);
}
