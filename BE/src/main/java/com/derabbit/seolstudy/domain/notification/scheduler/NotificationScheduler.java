package com.derabbit.seolstudy.domain.notification.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.derabbit.seolstudy.domain.notification.service.NotificationService;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TodoRepository todoRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void notifyIncompleteTodos() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        List<Todo> todos = todoRepository.findAllByDateAndState(targetDate, 0);
        for (Todo todo : todos) {
            notificationService.createTodoIncompleteNotification(todo, targetDate);
        }
    }
}
