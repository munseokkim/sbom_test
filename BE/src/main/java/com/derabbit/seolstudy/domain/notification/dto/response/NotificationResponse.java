package com.derabbit.seolstudy.domain.notification.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.derabbit.seolstudy.domain.notification.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String message;
    private String subject;
    private Long todoId;
    private Long fileId;
    private Long plannerId;
    private LocalDate plannerDate;
    private LocalDate targetDate;
    private LocalDateTime createdAt;
}
