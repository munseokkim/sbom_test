package com.derabbit.seolstudy.domain.planner.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.notification.service.NotificationService;
import com.derabbit.seolstudy.domain.planner.Planner;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerCommentResponse;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerStudySummaryResponse;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerSummaryResponse;
import com.derabbit.seolstudy.domain.planner.dto.response.PlannerTodoCountResponse;
import com.derabbit.seolstudy.domain.planner.repository.PlannerRepository;
import com.derabbit.seolstudy.domain.study.StudySession;
import com.derabbit.seolstudy.domain.study.repository.StudySessionRepository;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoWithMine;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final StudySessionRepository studySessionRepository;
    private final TodoRepository todoRepository;

    @Transactional
    public PlannerCommentResponse updateCommentByMentor(Long mentorId, Long plannerId, String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        Planner planner = plannerRepository.findById(plannerId)
                .orElseThrow(() -> new CustomException(ErrorCode.DATA_NOT_FOUND));

        User mentee = planner.getUser();

        validateMenteeAssignment(mentorId, mentee);

        planner.updateComment(comment);
        notificationService.createPlannerCommentNotification(mentee, planner);

        return PlannerCommentResponse.from(planner);
    }

    @Transactional(readOnly = true)
    public PlannerSummaryResponse getPlannerSummaryByMentee(Long menteeId, LocalDate date) {
        if (date == null) {
            throw new CustomException(ErrorCode.INVALID_DATE);
        }

        Planner planner = plannerRepository.findByUser_IdAndDate(menteeId, date).orElse(null);
        Long plannerId = planner == null ? null : planner.getId();
        String comment = planner == null ? null : planner.getComment();

        List<TodoWithMine> todos = todoRepository.findAllByUserIdAndFilters(menteeId, date, null, null);
        int total = todos.size();
        int completed = (int) todos.stream()
                .filter(todo -> todo.todo().getState() != null && todo.todo().getState() >= 1)
                .count();

        List<StudySession> sessions = studySessionRepository.findAllByUser_IdAndDateOrderByStartAtAsc(menteeId, date);
        Map<String, Long> bySubject = new HashMap<>();
        long totalSeconds = 0L;
        for (StudySession session : sessions) {
            if (session.getEndAt() == null || session.getDurationSeconds() == null) {
                continue;
            }
            Long seconds = session.getDurationSeconds();
            totalSeconds += seconds;
            bySubject.merge(session.getSubject(), seconds, Long::sum);
        }

        return PlannerSummaryResponse.builder()
                .plannerId(plannerId)
                .date(date)
                .comment(comment)
                .todoCounts(PlannerTodoCountResponse.of(total, completed))
                .studySummary(PlannerStudySummaryResponse.of(totalSeconds, bySubject))
                .build();
    }

    @Transactional(readOnly = true)
    public PlannerCommentResponse getCommentByMentor(Long mentorId, Long menteeId, LocalDate date) {
        if (date == null) {
            throw new CustomException(ErrorCode.INVALID_DATE);
        }

        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateMenteeAssignment(mentorId, mentee);

        Planner planner = plannerRepository.findByUser_IdAndDate(menteeId, date).orElse(null);
        return planner != null ? PlannerCommentResponse.from(planner) : PlannerCommentResponse.empty(date);
    }

    @Transactional
    public PlannerCommentResponse upsertCommentByMentor(Long mentorId, Long menteeId, LocalDate date, String comment) {
        if (date == null) {
            throw new CustomException(ErrorCode.INVALID_DATE);
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateMenteeAssignment(mentorId, mentee);

        Planner planner = plannerRepository.findByUser_IdAndDate(menteeId, date).orElse(null);
        boolean shouldNotify = false;
        if (planner == null) {
            planner = plannerRepository.save(Planner.create(mentee, date, comment));
            shouldNotify = true;
        } else if (!comment.equals(planner.getComment())) {
            planner.updateComment(comment);
            shouldNotify = true;
        }

        if (shouldNotify) {
            notificationService.createPlannerCommentNotification(mentee, planner);
        }

        return PlannerCommentResponse.from(planner);
    }

    private void validateMenteeAssignment(Long mentorId, User mentee) {
        if (mentee.getId() == null) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED);
        }

        if (mentee.getMentorId() == null || !mentorId.equals(mentee.getMentorId())) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED);
        }
    }
}
