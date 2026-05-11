package com.derabbit.seolstudy.domain.user.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.repository.FileRepository;
import com.derabbit.seolstudy.domain.feedback.repository.FeedbackRepository;
import com.derabbit.seolstudy.domain.notification.Notification;
import com.derabbit.seolstudy.domain.notification.NotificationType;
import com.derabbit.seolstudy.domain.notification.repository.NotificationRepository;
import com.derabbit.seolstudy.domain.study.StudySession;
import com.derabbit.seolstudy.domain.study.repository.StudySessionRepository;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.dto.request.UserUpdateRequest;
import com.derabbit.seolstudy.domain.user.dto.response.MenteeResponse;
import com.derabbit.seolstudy.domain.user.dto.response.MenteeSummaryResponse;
import com.derabbit.seolstudy.domain.user.dto.response.SubjectSummaryResponse;
import com.derabbit.seolstudy.domain.user.dto.response.UserResponse;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudySessionRepository studySessionRepository;
    private final TodoRepository todoRepository;
    private final NotificationRepository notificationRepository;
    private final FileRepository fileRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            user.updateProfile(name, null);
        }

        if (request.getSchool() != null) {
            String school = request.getSchool().trim();
            user.updateProfile(null, school.isEmpty() ? null : school);
        }

        if (request.getGrade() != null) {
            user.updateGrade(request.getGrade());
        }

        if (request.getNewPassword() != null || request.getCurrentPassword() != null) {
            String currentPassword = request.getCurrentPassword();
            String newPassword = request.getNewPassword();

            if (isBlank(currentPassword) || isBlank(newPassword)) {
                throw new CustomException(ErrorCode.INVALID_INPUT);
            }
            if (!currentPassword.equals(user.getPassword())) {
                throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
            }
            user.updatePassword(newPassword);
        }

        return new UserResponse(
                user.getEmail(),
                user.getName(),
                user.getSchool(),
                user.getGrade(),
                user.getRole().name()
        );
    }

    @Transactional
    public void assignMentee(Long mentorId, Long menteeId) {

        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        mentee.assignMentor(mentor);
    }

    @Transactional(readOnly = true)
    public List<MenteeResponse> getMyMentees(Long mentorId) {

        List<User> mentees = userRepository.findAllByMentorId(mentorId);

        return mentees.stream()
                .map(MenteeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new UserResponse(
                user.getEmail(),
                user.getName(),
                user.getSchool(),
                user.getGrade(),
                user.getRole().name()
        );
    }

    @Transactional(readOnly = true)
    public MenteeSummaryResponse getMenteeSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        LocalDate from = user.getCreatedAt() == null
                ? LocalDate.now()
                : user.getCreatedAt().toLocalDate();
        LocalDate to = LocalDate.now();

        Map<String, SubjectSummaryResponse> subjects = initSubjectSummary();
        if (to.isBefore(from)) {
            return MenteeSummaryResponse.of(from, to, subjects);
        }

        // Study time
        List<StudySession> sessions = studySessionRepository.findAllByUser_IdAndDateBetween(userId, from, to);
        for (StudySession session : sessions) {
            if (session.getDurationSeconds() == null || session.getEndAt() == null) {
                continue;
            }
            String key = normalizeSubject(session.getSubject());
            if (!subjects.containsKey(key)) {
                continue;
            }
            SubjectSummaryResponse summary = subjects.get(key);
            summary.addStudySeconds(session.getDurationSeconds());
        }

        // Todo completion rate
        List<Todo> todos = todoRepository.findAllByMentee_IdAndDateBetween(userId, from, to);
        for (Todo todo : todos) {
            String key = normalizeSubject(todo.getSubject());
            if (!subjects.containsKey(key)) {
                continue;
            }
            SubjectSummaryResponse summary = subjects.get(key);
            summary.incrementTodoTotal();
            if (todo.getState() == 2) {
                summary.incrementTodoCompleted();
            }
        }

        // Feedback read rate
        LocalDateTime fromAt = from.atStartOfDay();
        LocalDateTime toAt = to.plusDays(1).atStartOfDay();
        List<Notification> feedbacks = notificationRepository
                .findAllByUserIdAndTypeAndCreatedAtBetweenWithTodo(userId, NotificationType.FILE_FEEDBACK, fromAt, toAt);

        for (Notification notification : feedbacks) {
            if (notification.getTodo() == null) {
                continue;
            }
            String key = normalizeSubject(notification.getTodo().getSubject());
            if (!subjects.containsKey(key)) {
                continue;
            }
            SubjectSummaryResponse summary = subjects.get(key);
            summary.incrementFeedbackTotal();
            if (Boolean.TRUE.equals(notification.getIsRead())) {
                summary.incrementFeedbackRead();
            }
        }

        subjects.values().forEach(SubjectSummaryResponse::computeRates);
        return MenteeSummaryResponse.of(from, to, subjects);
    }

    @Transactional(readOnly = true)
    public MenteeSummaryResponse getMenteeSummaryForMentor(Long mentorId, Long menteeId, LocalDate date) {

        // 1) mentee 조회
        User mentee = userRepository.findById(menteeId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2) 권한 체크: 이 mentee의 mentor가 나인지 확인
        //    (assignMentor로 mentee에 mentor가 들어가는 구조라서 이렇게 체크하는게 제일 간단)
        if (mentee.getMentorId() == null || !mentee.getMentorId().equals(mentorId)) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED); // 없으면 ErrorCode 추가
        }

        // 3) 기간 계산: date 파라미터 있으면 해당 일자만, 없으면 가입일~오늘
        LocalDate from;
        LocalDate to;
        if (date != null) {
            from = date;
            to = date;
        } else {
            from = mentee.getCreatedAt() == null
                    ? LocalDate.now()
                    : mentee.getCreatedAt().toLocalDate();
            to = LocalDate.now();
        }

        Map<String, SubjectSummaryResponse> subjects = initSubjectSummary();
        if (to.isBefore(from)) {
            return MenteeSummaryResponse.of(from, to, subjects);
        }

        // 4) Study time(기존과 동일)
        List<StudySession> sessions = studySessionRepository.findAllByUser_IdAndDateBetween(menteeId, from, to);
        for (StudySession session : sessions) {
            if (session.getDurationSeconds() == null || session.getEndAt() == null) continue;
            String key = normalizeSubject(session.getSubject());
            if (!subjects.containsKey(key)) continue;
            subjects.get(key).addStudySeconds(session.getDurationSeconds());
        }

        // 5) Todo completion rate
        List<Todo> todos = todoRepository.findAllByMentee_IdAndDateBetween(menteeId, from, to);
        List<Long> todoIds = todos.stream().map(Todo::getId).toList();
        for (Todo todo : todos) {
            String key = normalizeSubject(todo.getSubject());
            if (!subjects.containsKey(key)) continue;
            SubjectSummaryResponse summary = subjects.get(key);
            summary.incrementTodoTotal();
            if (todo.getState() == 2) {
                summary.incrementTodoCompleted();
            }
        }

        // 6) 제출파일: 제출했지만 아직 피드백 미완료인 과제 수 (과목별)
        // 7) 피드백 작성 완료: 피드백이 이루어진 해결완료 과제 수 (과목별)
        if (!todoIds.isEmpty()) {
            List<File> menteeFiles = fileRepository.findByTodo_IdInAndCreator_Id(todoIds, menteeId);
            Set<Long> todoIdsWithMenteeFile = menteeFiles.stream()
                    .map(f -> f.getTodo().getId())
                    .collect(Collectors.toSet());
            Set<Long> todoIdsWithFeedback = feedbackRepository.findDistinctTodoIdsByFile_Todo_IdIn(todoIds);

            for (Todo todo : todos) {
                String key = normalizeSubject(todo.getSubject());
                if (!subjects.containsKey(key)) continue;
                boolean hasMenteeFile = todoIdsWithMenteeFile.contains(todo.getId());
                boolean hasFeedback = todoIdsWithFeedback.contains(todo.getId());
                if (hasMenteeFile && !hasFeedback) {
                    subjects.get(key).addPendingFeedbackTodoCount(1L);
                }
                if (hasFeedback) {
                    subjects.get(key).addFeedbackCompletedTodoCount(1L);
                }
            }
        }

        // 8) Feedback read rate(알림 기준, 기존 유지)
        LocalDateTime fromAt = from.atStartOfDay();
        LocalDateTime toAt = to.plusDays(1).atStartOfDay();
        List<Notification> feedbacks = notificationRepository
                .findAllByUserIdAndTypeAndCreatedAtBetweenWithTodo(
                        menteeId, NotificationType.FILE_FEEDBACK, fromAt, toAt
                );
        for (Notification notification : feedbacks) {
            if (notification.getTodo() == null) continue;
            String key = normalizeSubject(notification.getTodo().getSubject());
            if (!subjects.containsKey(key)) continue;
            SubjectSummaryResponse summary = subjects.get(key);
            summary.incrementFeedbackTotal();
            if (Boolean.TRUE.equals(notification.getIsRead())) {
                summary.incrementFeedbackRead();
            }
        }

        subjects.values().forEach(SubjectSummaryResponse::computeRates);
        return MenteeSummaryResponse.of(from, to, subjects);
    }

    private Map<String, SubjectSummaryResponse> initSubjectSummary() {
        Map<String, SubjectSummaryResponse> map = new LinkedHashMap<>();
        map.put("KOREAN", new SubjectSummaryResponse());
        map.put("ENGLISH", new SubjectSummaryResponse());
        map.put("MATH", new SubjectSummaryResponse());
        return map;
    }

    /** 항상 KOREAN/ENGLISH/MATH 중 하나 반환 → 과목별 버킷에 Todo가 빠지지 않도록 */
    private String normalizeSubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) {
            return "KOREAN";
        }
        String s = subject.trim();
        if ("KOREAN".equalsIgnoreCase(s) || "국어".equals(s)) return "KOREAN";
        if ("ENGLISH".equalsIgnoreCase(s) || "영어".equals(s)) return "ENGLISH";
        if ("MATH".equalsIgnoreCase(s) || "수학".equals(s)) return "MATH";
        return "KOREAN";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
