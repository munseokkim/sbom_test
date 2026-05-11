package com.derabbit.seolstudy.domain.study;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.derabbit.seolstudy.domain.common.BaseTimeEntity;
import com.derabbit.seolstudy.domain.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "study_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySession extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 50)
    private String subject;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StudySessionMode mode;

    public static StudySession create(
            User user,
            LocalDate date,
            String subject,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long durationSeconds,
            StudySessionMode mode
    ) {
        StudySession session = new StudySession();
        session.user = user;
        session.date = date;
        session.subject = subject;
        session.startAt = startAt;
        session.endAt = endAt;
        session.durationSeconds = durationSeconds;
        session.mode = mode;
        return session;
    }

    public void stop(LocalDateTime endAt, Long durationSeconds) {
        this.endAt = endAt;
        this.durationSeconds = durationSeconds;
    }
}
