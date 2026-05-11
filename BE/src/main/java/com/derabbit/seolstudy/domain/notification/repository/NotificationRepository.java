package com.derabbit.seolstudy.domain.notification.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.derabbit.seolstudy.domain.notification.Notification;
import com.derabbit.seolstudy.domain.notification.NotificationType;
import com.derabbit.seolstudy.domain.todo.Todo;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    List<Notification> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    boolean existsByTypeAndTodoAndCreatedAtAfter(NotificationType type, Todo todo, LocalDateTime after);

    boolean existsByTypeAndTodo(NotificationType type, Todo todo);

    void deleteByTypeAndTodo(NotificationType type, Todo todo);

    @Query("""
            select n
            from Notification n
            join fetch n.todo t
            where n.user.id = :userId
              and n.type = :type
              and n.createdAt >= :from
              and n.createdAt < :to
            """)
    List<Notification> findAllByUserIdAndTypeAndCreatedAtBetweenWithTodo(
            @Param("userId") Long userId,
            @Param("type") NotificationType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
