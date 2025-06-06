package com.moa.moa_server.support;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import java.time.LocalDateTime;

/** 테스트용 Vote 엔티티 객체를 생성하는 팩토리 클래스 */
public class VoteTestFactory {

  /** 테스트용 더미 Vote 엔티티 생성 */
  public static Vote createDummy(User user, Group group) {
    return Vote.builder()
        .user(user)
        .group(group)
        .content("테스트 투표")
        .imageUrl(null)
        .closedAt(LocalDateTime.now().plusDays(1))
        .anonymous(false)
        .voteStatus(Vote.VoteStatus.OPEN)
        .adminVote(false)
        .voteType(Vote.VoteType.USER)
        .lastAnonymousNumber(0)
        .build();
  }
}
