package com.moa.moa_server.domain.user.dto.response;

import java.util.List;

public record JoinedGroupResponse(
    List<GroupDetail> groups,
    String nextCursor,
    boolean hasNext,
    int size
) {}