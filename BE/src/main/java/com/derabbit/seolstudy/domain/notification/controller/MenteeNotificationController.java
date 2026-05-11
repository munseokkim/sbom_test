package com.derabbit.seolstudy.domain.notification.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.notification.dto.response.NotificationListResponse;
import com.derabbit.seolstudy.domain.notification.dto.response.NotificationReadResponse;
import com.derabbit.seolstudy.domain.notification.service.NotificationService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentee/notifications")
public class MenteeNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationListResponse getNotifications(
            @RequestParam(name = "unreadOnly", defaultValue = "true") boolean unreadOnly,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return notificationService.getNotifications(userId, unreadOnly);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationReadResponse markRead(
            @PathVariable("notificationId") Long notificationId,
            Authentication authentication
    ) {
        Long userId = getCurrentUserId(authentication);
        return notificationService.markRead(userId, notificationId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
