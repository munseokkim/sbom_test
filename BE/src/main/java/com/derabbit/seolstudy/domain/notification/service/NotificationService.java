package com.derabbit.seolstudy.domain.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.repository.FileRepository;
import com.derabbit.seolstudy.domain.notification.Notification;
import com.derabbit.seolstudy.domain.notification.NotificationType;
import com.derabbit.seolstudy.domain.notification.dto.response.NotificationListResponse;
import com.derabbit.seolstudy.domain.notification.dto.response.NotificationReadResponse;
import com.derabbit.seolstudy.domain.notification.dto.response.NotificationResponse;
import com.derabbit.seolstudy.domain.notification.repository.NotificationRepository;
import com.derabbit.seolstudy.domain.planner.Planner;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String MESSAGE_TODO_FEEDBACK = "과제 피드백이 달렸어요";
    private static final String MESSAGE_PLANNER_FEEDBACK = "플래너 피드백이 달렸어요";
    private static final String MESSAGE_TODO_INCOMPLETE = "오늘 과제를 아직 제출하지 않았어요";

    private final NotificationRepository notificationRepository;
    private final FileRepository fileRepository;
    private final TodoRepository todoRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(Long userId, boolean unreadOnly) {
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findAllByUserIdAndIsReadOrderByCreatedAtDesc(userId, Boolean.FALSE)
                : notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        List<NotificationResponse> items = notifications.stream()
                .map(this::toResponse)
                .toList();

        return NotificationListResponse.from(items);
    }

    @Transactional
    public void sendFileFeedbackNotification(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        createFileFeedbackNotification(file);
    }

    @Transactional
    public void sendTodoFeedbackNotification(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        createTodoFeedbackNotification(todo);
    }

    @Transactional
    public void createTodoFeedbackNotification(Todo todo) {
        User mentee = todo.getMentee();
        Notification notification = Notification.todoFeedback(mentee, todo, MESSAGE_TODO_FEEDBACK);
        notificationRepository.save(notification);
    }

    @Transactional
    public NotificationReadResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        notification.markRead();

        return NotificationReadResponse.from(notification.getId(), notification.getIsRead());
    }

    @Transactional
    public void createTodoCommentNotification(Todo todo) {
        User mentee = todo.getMentee();
        Notification notification = Notification.todoComment(mentee, todo, MESSAGE_TODO_FEEDBACK);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createFileFeedbackNotification(File file) {
        Todo todo = file.getTodo();
        User mentee = todo.getMentee();
        Notification notification = Notification.fileFeedback(mentee, todo, file, MESSAGE_TODO_FEEDBACK);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createPlannerCommentNotification(User mentee, Planner planner) {
        Notification notification = Notification.plannerComment(mentee, planner, MESSAGE_PLANNER_FEEDBACK);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createTodoIncompleteNotification(Todo todo, LocalDate targetDate) {
        if (notificationRepository.existsByTypeAndTodo(NotificationType.TODO_INCOMPLETE, todo)) {
            return;
        }
        User mentee = todo.getMentee();
        Notification notification = Notification.todoIncomplete(mentee, todo, targetDate, MESSAGE_TODO_INCOMPLETE);
        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteTodoIncompleteNotification(Todo todo) {
        notificationRepository.deleteByTypeAndTodo(NotificationType.TODO_INCOMPLETE, todo);
    }

    private NotificationResponse toResponse(Notification notification) {
        Todo todo = notification.getTodo();
        File file = notification.getFile();
        Planner planner = notification.getPlanner();

        String subject = null;
        Long todoId = null;
        if (todo != null) {
            todoId = todo.getId();
            subject = todo.getSubject();
        } else if (file != null && file.getTodo() != null) {
            todoId = file.getTodo().getId();
            subject = file.getTodo().getSubject();
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .subject(subject)
                .todoId(todoId)
                .fileId(file == null ? null : file.getId())
                .plannerId(planner == null ? null : planner.getId())
                .plannerDate(planner == null ? null : planner.getDate())
                .targetDate(notification.getTargetDate())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
