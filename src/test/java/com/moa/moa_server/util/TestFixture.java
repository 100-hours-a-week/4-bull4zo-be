package com.moa.moa_server.util;

import com.moa.moa_server.domain.comment.entity.Comment;
import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.entity.GroupMember;
import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;
import com.moa.moa_server.domain.vote.entity.VoteResponse;
import java.time.LocalDateTime;
import java.util.UUID;

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
    String inviteCode = UUID.randomUUID().toString().substring(0, 6);
    return Group.builder()
        .name(name)
        .inviteCode(inviteCode)
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

  public static Comment comment(Vote vote, User user, String content, int anonymousNumber) {
    return Comment.create(user, vote, content, anonymousNumber);
  }

  public static GroupMember groupMember(User user, Group group, String role) {
    if (role.equals("OWNER")) {
      return GroupMember.createAsOwner(user, group);
    } else if (role.equals("MEMBER")) {
      return GroupMember.create(user, group);
    }
    throw new IllegalArgumentException("Invalid role: " + role);
  }
}
