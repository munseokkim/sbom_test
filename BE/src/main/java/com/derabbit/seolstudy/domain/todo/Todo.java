package com.derabbit.seolstudy.domain.todo;

import java.time.LocalDate;

import com.derabbit.seolstudy.domain.common.BaseTimeEntity;
import com.derabbit.seolstudy.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User mentee; // 실제 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator; // 만든 사람

    @Column(nullable = false, length = 100)
    private String title;

    private LocalDate date;

    private String subject;

    @Lob
    private String goal;

    private Integer state;

    @Lob
    private String comment;

    public static Todo create(
            User mentee,
            User creator,
            String title,
            LocalDate date,
            String subject,
            String goal,
            Integer state,
            String comment
    ) {
        Todo todo = new Todo();
        todo.mentee = mentee;
        todo.creator = creator;
        todo.title = title;
        todo.date = date;
        todo.subject = subject;
        todo.goal = goal;
        todo.state = state;
        todo.comment = comment;
        return todo;
    }

    public void updateByMentee(
            String title,
            LocalDate date,
            String subject,
            String goal,
            Integer state
    ) {
        this.title = title;
        this.date = date;
        this.subject = subject;
        this.goal = goal;
        this.state = state;
    }

    public void updateByMentor(
            String title,
            LocalDate date,
            String subject,
            String goal
    ) {
        this.title = title;
        this.date = date;
        this.subject = subject;
        this.goal = goal;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void updateState(Integer state) {
        this.state = state;
    }

}
