package com.derabbit.seolstudy.domain.todo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.derabbit.seolstudy.domain.todo.dto.request.MentorCreateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.MentorUpdateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.TodoCommentRequest;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoDetailResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoSummaryResponse;
import com.derabbit.seolstudy.domain.todo.service.TodoService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentor")
public class MentorTodoController {

    private final TodoService todoService;

    @PostMapping("/mentees/{menteeId}/todos")
    public TodoResponse createForMentee(
            @PathVariable("menteeId") Long menteeId,
            @RequestBody MentorCreateTodoRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return todoService.createByMentor(mentorId, menteeId, request);
    }

    @GetMapping("/mentees/{menteeId}/todos")
    public List<TodoSummaryResponse> getMenteeList(
            @PathVariable("menteeId") Long menteeId,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "state", required = false) Integer state,
            @RequestParam(name = "subject", required = false) String subject,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return todoService.getMentorMenteeTodos(mentorId, menteeId, date, state, subject);
    }

    @GetMapping("/todos/{todoId}")
    public TodoDetailResponse getDetail(@PathVariable("todoId") Long todoId, Authentication authentication) {
        Long mentorId = getCurrentUserId(authentication);
        return todoService.getMentorTodoDetail(mentorId, todoId);
    }

    @PatchMapping("/todos/{todoId}")
    public TodoResponse update(
            @PathVariable("todoId") Long todoId,
            @RequestBody MentorUpdateTodoRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return todoService.updateByMentor(mentorId, todoId, request);
    }

    @PatchMapping("/todos/{todoId}/comment")
    public TodoResponse updateComment(
            @PathVariable("todoId") Long todoId,
            @RequestBody TodoCommentRequest request,
            Authentication authentication
    ) {
        Long mentorId = getCurrentUserId(authentication);
        return todoService.updateCommentByMentor(mentorId, todoId, request.getComment());
    }

    @DeleteMapping("/todos/{todoId}")
    public ResponseEntity<Void> delete(@PathVariable("todoId") Long todoId, Authentication authentication) {
        Long mentorId = getCurrentUserId(authentication);
        todoService.deleteByMentor(mentorId, todoId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
