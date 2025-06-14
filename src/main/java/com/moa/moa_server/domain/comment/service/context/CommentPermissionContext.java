package com.moa.moa_server.domain.comment.service.context;

import com.moa.moa_server.domain.user.entity.User;
import com.moa.moa_server.domain.vote.entity.Vote;

public record CommentPermissionContext(User user, Vote vote) {}
