package com.moa.moa_server.domain.ranking.util;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupMemberRepository;
import com.moa.moa_server.domain.ranking.service.RankingRedisService;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingPermissionValidator {

  private final GroupMemberRepository groupMemberRepository;
  private final RankingRedisService rankingRedisService;

  public boolean isAccessibleAsTopRankedVote(User user, Vote vote) {
    // top3 투표는 그룹 멤버인 경우 허용
    if (!rankingRedisService.isTopRankedVote(vote)) return false;
    Group group = vote.getGroup();
    return group.isPublicGroup() || groupMemberRepository.existsByGroupAndUser(group, user);
  }
}
