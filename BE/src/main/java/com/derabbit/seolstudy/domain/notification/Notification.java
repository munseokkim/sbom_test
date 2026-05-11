package com.derabbit.seolstudy.domain.notification;

import java.time.LocalDate;

import com.derabbit.seolstudy.domain.common.BaseTimeEntity;
import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.planner.Planner;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_user_id", columnList = "user_id"),
                @Index(name = "idx_notification_todo_id", columnList = "todo_id"),
                @Index(name = "idx_notification_planner_id", columnList = "planner_id"),
                @Index(name = "idx_notification_file_id", columnList = "file_id"),
                @Index(name = "idx_notification_user_read", columnList = "user_id,is_read")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planner_id")
    private Planner planner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Lob
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    public static Notification todoComment(User user, Todo todo, String message) {
        Notification notification = new Notification();
        notification.user = user;
        notification.todo = todo;
        notification.type = NotificationType.TODO_COMMENT;
        notification.message = message;
        return notification;
    }

    public static Notification fileFeedback(User user, Todo todo, File file, String message) {
        Notification notification = new Notification();
        notification.user = user;
        notification.todo = todo;
        notification.file = file;
        notification.type = NotificationType.FILE_FEEDBACK;
        notification.message = message;
        return notification;
    }

    public static Notification plannerComment(User user, Planner planner, String message) {
        Notification notification = new Notification();
        notification.user = user;
        notification.planner = planner;
        notification.type = NotificationType.PLANNER_COMMENT;
        notification.message = message;
        return notification;
    }

    public static Notification todoIncomplete(User user, Todo todo, LocalDate targetDate, String message) {
        Notification notification = new Notification();
        notification.user = user;
        notification.todo = todo;
        notification.targetDate = targetDate;
        notification.type = NotificationType.TODO_INCOMPLETE;
        notification.message = message;
        return notification;
    }

    public static Notification todoFeedback(User user, Todo todo, String message) {
        Notification notification = new Notification();
        notification.user = user;
        notification.todo = todo;
        notification.type = NotificationType.FILE_FEEDBACK; // "피드백이 달림" 알림의 타입은 FILE_FEEDBACK 유지
        notification.message = message;
        return notification;
    }

    public void markRead() {
        this.isRead = true;
    }
}
