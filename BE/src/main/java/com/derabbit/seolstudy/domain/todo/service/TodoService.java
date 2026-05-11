package com.derabbit.seolstudy.domain.todo.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.derabbit.seolstudy.domain.todo.dto.response.TodoWithMine;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.derabbit.seolstudy.domain.feedback.Feedback;
import com.derabbit.seolstudy.domain.feedback.repository.FeedbackRepository;
import com.derabbit.seolstudy.domain.file.File;
import com.derabbit.seolstudy.domain.file.repository.FileRepository;
import com.derabbit.seolstudy.domain.notification.service.NotificationService;
import com.derabbit.seolstudy.domain.todo.Todo;
import com.derabbit.seolstudy.domain.todo.dto.request.CreateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.MentorCreateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.MentorUpdateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.UpdateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoDetailResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoFeedbackResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoFileResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoSummaryResponse;
import com.derabbit.seolstudy.domain.todo.repository.TodoRepository;
import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationService notificationService;

    @Transactional
    public TodoResponse createByMentee(Long menteeId, CreateTodoRequest request) {
        validateCreateRequest(request);

        User mentee = userRepository.findById(menteeId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Todo todo = Todo.create(
                mentee,
                mentee,
                request.getTitle().trim(),
                request.getDate(),
                trimToNull(request.getSubject()),
                request.getGoal(),
                0,
                null
        );

        return TodoResponse.from(todoRepository.save(todo));
    }

    @Transactional
    public TodoResponse createByMentor(Long mentorId, Long menteeId, MentorCreateTodoRequest request) {
        validateCreateRequest(request);

        User mentee = userRepository.findById(menteeId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        User mentor = userRepository.findById(mentorId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateMenteeAssignment(mentorId, mentee);

        Todo todo = Todo.create(
                mentee,
                mentor,
                request.getTitle().trim(),
                request.getDate(),
                trimToNull(request.getSubject()),
                request.getGoal(),
                0,
                null
        );

        return TodoResponse.from(todoRepository.save(todo));
    }

    public List<TodoSummaryResponse> getMenteeTodos(Long menteeId, LocalDate date, Integer state, String subject) {
        List<TodoWithMine> list = todoRepository.findAllByUserIdAndFilters(menteeId, date, state, trimToNull(subject));
        if (list.isEmpty()) {
            return List.of();
        }
        List<Long> todoIds = list.stream().map(t -> t.todo().getId()).toList();
        List<Object[]> countRows = fileRepository.countByTodoIdIn(todoIds);
        Map<Long, Long> countMap = new HashMap<>();
        for (Object[] row : countRows) {
            Long todoId = (Long) row[0];
            Number cnt = (Number) row[1];
            if (todoId != null) {
                countMap.put(todoId, cnt != null ? cnt.longValue() : 0L);
            }
        }
        return list.stream()
                .map(t -> TodoSummaryResponse.from(t, countMap.getOrDefault(t.todo().getId(), 0L)))
                .toList();
    }

    public List<TodoSummaryResponse> getMentorMenteeTodos(Long mentorId, Long menteeId, LocalDate date, Integer state, String subject) {
        
        User mentee = userRepository.findById(menteeId)
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateMenteeAssignment(mentorId, mentee);
        
        return getMenteeTodos(menteeId, date, state, subject);
    }

    public TodoDetailResponse getMenteeTodoDetail(Long menteeId, Long todoId) {
        Todo todo = getTodoOrThrow(todoId);
        if (!menteeId.equals(todo.getMentee().getId())) {
            throw new CustomException(ErrorCode.TODO_NOT_FOUND);
        }
        return toTodoDetailResponse(todo);
    }

    public TodoDetailResponse getMentorTodoDetail(Long mentorId, Long todoId) {
        Todo todo = getTodoOrThrow(todoId);
        validateMenteeAssignment(mentorId, todo.getMentee());
        return toTodoDetailResponse(todo);
    }

    @Transactional
    public TodoResponse updateByMentee(Long menteeId, Long todoId, UpdateTodoRequest request) {
        validateUpdateRequest(request, true);
        Todo todo = getTodoOrThrow(todoId);

        // 담당 멘티인지 확인 (멘토가 생성한 할 일도 멘티는 수정·완료 처리 가능)
        if (!menteeId.equals(todo.getMentee().getId())) {
            throw new CustomException(ErrorCode.TODO_NOT_FOUND);
        }

        todo.updateByMentee(
                request.getTitle().trim(),
                request.getDate(),
                trimToNull(request.getSubject()),
                request.getGoal(),
                request.getState()
        );

        if (request.getState() != 0) {
            notificationService.deleteTodoIncompleteNotification(todo);
        }

        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse updateCommentByMentor(Long mentorId, Long todoId, String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        Todo todo = getTodoOrThrow(todoId);
        validateMenteeAssignment(mentorId, todo.getMentee());

        todo.updateComment(comment);
        notificationService.createTodoCommentNotification(todo);

        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse updateByMentor(Long mentorId, Long todoId, MentorUpdateTodoRequest request) {
        validateUpdateRequest(request);
        Todo todo = getTodoOrThrow(todoId);

        validateMenteeAssignment(mentorId, todo.getMentee());

        todo.updateByMentor(
                request.getTitle().trim(),
                request.getDate(),
                trimToNull(request.getSubject()),
                request.getGoal()
        );

        return TodoResponse.from(todo);
    }

    @Transactional
    public void deleteByMentee(Long menteeId, Long todoId) {
        Todo todo = getTodoOrThrow(todoId);

        if (!menteeId.equals(todo.getMentee().getId())) {
            throw new CustomException(ErrorCode.TODO_NOT_FOUND);
        }
        if (!menteeId.equals(todo.getCreator().getId())) {
            throw new CustomException(ErrorCode.TODO_DELETE_FORBIDDEN);
        }

        deleteTodoWithFilesAndFeedbacks(todo);
    }

    @Transactional
    public void deleteByMentor(Long mentorId, Long todoId) {
        Todo todo = getTodoOrThrow(todoId);

        validateMenteeAssignment(mentorId, todo.getMentee());

        deleteTodoWithFilesAndFeedbacks(todo);
    }

    /** FK 제약 때문에 Todo 삭제 전에 Feedback → File 순으로 삭제 */
    private void deleteTodoWithFilesAndFeedbacks(Todo todo) {
        List<File> files = fileRepository.findByTodo_Id(todo.getId());
        if (!files.isEmpty()) {
            List<Feedback> feedbacks = feedbackRepository.findAllByFileIn(files);
            feedbackRepository.deleteAll(feedbacks);
            fileRepository.deleteAll(files);
        }
        todoRepository.delete(todo);
    }

    private Todo getTodoOrThrow(Long todoId) {
        return todoRepository.findById(todoId)
                .orElseThrow(() -> new CustomException(ErrorCode.TODO_NOT_FOUND));
    }

    private void validateMenteeAssignment(Long mentorId, User mentee) {
        if (mentee.getId() == null) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED);
        }

        if (mentee.getMentorId() == null || !mentorId.equals(mentee.getMentorId())) {
            throw new CustomException(ErrorCode.MENTEE_NOT_ASSIGNED);
        }
    }

    private TodoDetailResponse toTodoDetailResponse(Todo todo) {
        return TodoDetailResponse.from(todo, buildFileResponses(todo));
    }

    private List<TodoFileResponse> buildFileResponses(Todo todoId) {
        List<File> files = fileRepository.findAllByTodoOrderByCreatedAtAsc(todoId);
        if (files.isEmpty()) {
            return List.of();
        }

        Map<File, List<TodoFeedbackResponse>> feedbackMap = feedbackRepository.findAllByFileIn(files)
                .stream()
                .collect(Collectors.groupingBy(
                        Feedback::getFile,
                        Collectors.mapping(TodoFeedbackResponse::from, Collectors.toList())
                ));

        return files.stream()
                .map(file -> TodoFileResponse.from(file, feedbackMap.getOrDefault(file, List.of())))
                .toList();
    }

    private void validateCreateRequest(CreateTodoRequest request) {
        if (request == null || isBlank(request.getTitle())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (request.getTitle().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateCreateRequest(MentorCreateTodoRequest request) {
        if (request == null || isBlank(request.getTitle())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (request.getTitle().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private void validateUpdateRequest(UpdateTodoRequest request, boolean allowStatusUpdate) {
        if (request == null || isBlank(request.getTitle())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (request.getTitle().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (!allowStatusUpdate && request.getState() != null) {
            throw new CustomException(ErrorCode.TODO_EDIT_FORBIDDEN);
        }
    }

    private void validateUpdateRequest(MentorUpdateTodoRequest request) {
        if (request == null || isBlank(request.getTitle())) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
        if (request.getTitle().length() > 100) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
