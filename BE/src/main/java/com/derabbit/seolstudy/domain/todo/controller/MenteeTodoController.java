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

import com.derabbit.seolstudy.domain.todo.dto.request.CreateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.request.UpdateTodoRequest;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoDetailResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoResponse;
import com.derabbit.seolstudy.domain.todo.dto.response.TodoSummaryResponse;
import com.derabbit.seolstudy.domain.todo.service.TodoService;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mentee/todos")
public class MenteeTodoController {

    private final TodoService todoService;

    @PostMapping
    public TodoResponse create(@RequestBody CreateTodoRequest request, Authentication authentication) {
        Long menteeId = getCurrentUserId(authentication);
        return todoService.createByMentee(menteeId, request);
    }

    @GetMapping
    public List<TodoSummaryResponse> getList(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "state", required = false) Integer state,
            @RequestParam(name = "subject", required = false) String subject,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return todoService.getMenteeTodos(menteeId, date, state, subject);
    }

    @GetMapping("/{todoId}")
    public TodoDetailResponse getDetail(
        @PathVariable("todoId") 
        Long todoId, 
        Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return todoService.getMenteeTodoDetail(menteeId, todoId);
    }

    @PatchMapping("/{todoId}")
    public TodoResponse update(
            @PathVariable("todoId") Long todoId,
            @RequestBody UpdateTodoRequest request,
            Authentication authentication
    ) {
        Long menteeId = getCurrentUserId(authentication);
        return todoService.updateByMentee(menteeId, todoId, request);
    }

    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> delete(@PathVariable("todoId") Long todoId, Authentication authentication) {
        Long menteeId = getCurrentUserId(authentication);
        todoService.deleteByMentee(menteeId, todoId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new CustomException(ErrorCode.AUTH_REQUIRED);
        }
        return userId;
    }
}
