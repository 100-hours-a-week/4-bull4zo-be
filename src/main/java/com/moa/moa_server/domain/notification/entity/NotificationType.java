package com.moa.moa_server.domain.notification.entity;

public enum NotificationType {
  MY_VOTE_CLOSED("vote", "내 투표가 종료되었습니다."), // 내가 만든 투표 종료
  SUBMITTED_VOTE_CLOSED("vote", ""), // 참여한 투표 종료
  VOTE_APPROVED("vote", "투표가 등록 되었습니다."), // 투표 등록 성공
  VOTE_REJECTED("vote", "투표 등록이 실패했습니다."), // 투표 등록 실패
  MY_VOTE_COMMENT("comment", "투표에 댓글이 달렸습니다. 확인해보세요!"), // 내 투표의 댓글
  TOP3_UPDATED("system", "오늘의 Top3 투표를 확인해보세요."), // Top3 투표 알림
  GROUP_DELETED("group", "그룹이 삭제되었습니다."); // 가입한 그룹 삭제

  private final String domain;
  private final String defaultMessage;

  NotificationType(String domain, String defaultMessage) {
    this.domain = domain;
    this.defaultMessage = defaultMessage;
  }

  public String getDomain() {
    return domain;
  }

  public String getDefaultMessage() {
    return defaultMessage;
  }
}
