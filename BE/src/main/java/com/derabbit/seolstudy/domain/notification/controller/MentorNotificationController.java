package com.derabbit.seolstudy.domain.notification.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor/notifications")
public class MentorNotificationController {

    private final NotificationService notificationService;

    @PostMapping("/feedback/todo/{todoId}")
    public void sendTodoFeedbackNotification(@PathVariable("todoId") Long todoId) {
        notificationService.sendTodoFeedbackNotification(todoId);
    }
}
