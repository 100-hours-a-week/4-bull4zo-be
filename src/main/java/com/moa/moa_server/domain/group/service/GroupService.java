package com.moa.moa_server.domain.group.service;

import com.moa.moa_server.domain.group.entity.Group;
import com.moa.moa_server.domain.group.repository.GroupRepository;
import com.moa.moa_server.domain.vote.handler.VoteErrorCode;
import com.moa.moa_server.domain.vote.handler.VoteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    public Group getPublicGroup() {
        return groupRepository.findById(1L)
                .orElseThrow(() -> new VoteException(VoteErrorCode.GROUP_NOT_FOUND));
    }
}
