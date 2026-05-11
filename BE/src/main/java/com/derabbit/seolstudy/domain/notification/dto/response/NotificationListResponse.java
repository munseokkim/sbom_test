package com.derabbit.seolstudy.domain.notification.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationListResponse {

    private List<NotificationResponse> items;

    public static NotificationListResponse from(List<NotificationResponse> items) {
        return NotificationListResponse.builder()
                .items(items)
                .build();
    }
}
