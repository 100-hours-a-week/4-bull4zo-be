package com.moa.moa_server.util;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import java.time.LocalDateTime;

public class TestFixture {

  public static User user(String nickname) {
    return User.builder()
        .nickname(nickname)
        .role(User.Role.USER)
        .userStatus(User.UserStatus.ACTIVE)
        .lastActiveAt(LocalDateTime.now())
        .build();
  }

  public static Group group(User user, String name) {
    return Group.builder()
        .name(name)
        .inviteCode("000000")
        .user(user)
        .description("테스트 그룹입니다.")
        .build();
  }

  public static Vote vote(User user, Group group, Vote.VoteStatus voteStatus) {
    return Vote.createUserVote(
        user, group, "본문입니다", "", "", LocalDateTime.now().plusDays(1), false, voteStatus, false);
  }

  public static VoteResponse voteResponse(Vote vote, User user, int option) {
    return VoteResponse.builder().vote(vote).user(user).optionNumber(option).build();
  }
}
