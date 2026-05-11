package com.derabbit.seolstudy.domain.study.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.study.StudySession;
import com.derabbit.seolstudy.domain.study.StudySessionMode;
import com.derabbit.seolstudy.domain.study.dto.request.StudySessionManualRequest;
import com.derabbit.seolstudy.domain.study.dto.request.StudySessionStartRequest;
import com.derabbit.seolstudy.domain.study.dto.request.StudySessionStopRequest;
import com.derabbit.seolstudy.domain.study.dto.response.StudySessionBatchResponse;
import com.derabbit.seolstudy.domain.study.dto.response.StudySessionResponse;
import com.derabbit.seolstudy.domain.study.repository.StudySessionRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final UserRepository userRepository;

    @Transactional
    public StudySessionResponse startAutoSession(Long userId, StudySessionStartRequest request) {
        String subject = normalizeSubject(request == null ? null : request.getSubject());
        LocalDateTime startAt = request == null ? null : request.getStartAt();
        if (startAt == null) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }
        if (studySessionRepository.existsByUser_IdAndEndAtIsNull(userId)) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudySession session = StudySession.create(
                user,
                startAt.toLocalDate(),
                subject,
                startAt,
                null,
                null,
                StudySessionMode.AUTO
        );

        return StudySessionResponse.from(studySessionRepository.save(session));
    }

    @Transactional
    public StudySessionBatchResponse stopAutoSession(Long userId, Long sessionId, StudySessionStopRequest request) {
        if (request == null || request.getEndAt() == null) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        if (session.getEndAt() != null || session.getMode() != StudySessionMode.AUTO) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }

        List<StudySession> updated = splitAndSaveAutoSession(session, request.getEndAt());
        return StudySessionBatchResponse.from(updated.stream().map(StudySessionResponse::from).toList());
    }

    @Transactional
    public StudySessionBatchResponse createManualSession(Long userId, StudySessionManualRequest request) {
        String subject = normalizeSubject(request == null ? null : request.getSubject());
        LocalDateTime startAt = request == null ? null : request.getStartAt();
        LocalDateTime endAt = request == null ? null : request.getEndAt();
        if (startAt == null || endAt == null) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }

        List<TimeSegment> segments = splitSegments(startAt, endAt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        List<StudySession> saved = new ArrayList<>();
        for (TimeSegment segment : segments) {
            Long seconds = secondsBetween(segment.startAt, segment.endAt);
            StudySession session = StudySession.create(
                    user,
                    segment.startAt.toLocalDate(),
                    subject,
                    segment.startAt,
                    segment.endAt,
                    seconds,
                    StudySessionMode.MANUAL
            );
            saved.add(studySessionRepository.save(session));
        }

        return StudySessionBatchResponse.from(saved.stream().map(StudySessionResponse::from).toList());
    }

    public List<StudySessionResponse> getSessionsByDate(Long userId, LocalDate date) {
        if (date == null) {
            throw new CustomException(ErrorCode.INVALID_DATE);
        }
        return studySessionRepository.findAllByUser_IdAndDateOrderByStartAtAsc(userId, date)
                .stream()
                .map(StudySessionResponse::from)
                .toList();
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        StudySession session = studySessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));
        studySessionRepository.delete(session);
    }

    private List<StudySession> splitAndSaveAutoSession(StudySession session, LocalDateTime endAt) {
        List<TimeSegment> segments = splitSegments(session.getStartAt(), endAt);
        if (segments.isEmpty()) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }

        List<StudySession> result = new ArrayList<>();
        TimeSegment first = segments.get(0);
        session.stop(first.endAt, secondsBetween(first.startAt, first.endAt));
        result.add(session);

        for (int i = 1; i < segments.size(); i++) {
            TimeSegment segment = segments.get(i);
            StudySession newSession = StudySession.create(
                    session.getUser(),
                    segment.startAt.toLocalDate(),
                    session.getSubject(),
                    segment.startAt,
                    segment.endAt,
                    secondsBetween(segment.startAt, segment.endAt),
                    session.getMode()
            );
            result.add(studySessionRepository.save(newSession));
        }

        return result;
    }

    private List<TimeSegment> splitSegments(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new CustomException(ErrorCode.STUDY_TIME_INVALID);
        }

        List<TimeSegment> segments = new ArrayList<>();
        LocalDateTime segmentStart = startAt;

        while (segmentStart.toLocalDate().isBefore(endAt.toLocalDate())) {
            LocalDateTime nextMidnight = segmentStart.toLocalDate().plusDays(1).atStartOfDay();
            segments.add(new TimeSegment(segmentStart, nextMidnight));
            segmentStart = nextMidnight;
        }

        if (segmentStart.isBefore(endAt)) {
            segments.add(new TimeSegment(segmentStart, endAt));
        }

        return segments;
    }

    private Long secondsBetween(LocalDateTime startAt, LocalDateTime endAt) {
        return Duration.between(startAt, endAt).getSeconds();
    }

    private String normalizeSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        return subject.trim();
    }

    private static class TimeSegment {
        private final LocalDateTime startAt;
        private final LocalDateTime endAt;

        private TimeSegment(LocalDateTime startAt, LocalDateTime endAt) {
            this.startAt = startAt;
            this.endAt = endAt;
        }
    }
}
