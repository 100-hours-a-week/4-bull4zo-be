package com.moa.moa_server.domain.comment.util;

import com.moa.moa_server.domain.comment.entity.Comment;

public class CommentNicknameUtil {
  /** 댓글/유저/익명여부 기반 닉네임 생성 */
  public static String generateNickname(boolean anonymous, int anonymousNumber, String nickname) {
    return anonymous ? "익명" + anonymousNumber : nickname;
  }

  /** Comment 엔티티 기반 닉네임 생성 (CommentItem.of에서 사용) */
  public static String fromComment(Comment comment) {
    return comment.getAnonymousNumber() > 0
        ? "익명" + comment.getAnonymousNumber()
        : comment.getUser().getNickname();
  }
}
