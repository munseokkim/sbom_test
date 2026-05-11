package com.derabbit.seolstudy.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationReadResponse {

    private Long id;
    private Boolean isRead;

    public static NotificationReadResponse from(Long id, Boolean isRead) {
        return NotificationReadResponse.builder()
                .id(id)
                .isRead(isRead)
                .build();
    }
}
