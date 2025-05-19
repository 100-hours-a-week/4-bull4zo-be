package com.moa.moa_server.domain.user.dto.response;

import com.moa.moa_server.domain.group.entity.Group;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 라벨")
public record GroupLabel(
        @Schema(description = "그룹 ID", example = "1")
        Long groupId,

        @Schema(description = "그룹 이름", example = "공개")
        String name
) {

    public static GroupLabel from(Group group) {
        return new GroupLabel(group.getId(), group.getName());
    }
}
