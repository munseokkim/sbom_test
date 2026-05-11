package com.derabbit.seolstudy.domain.todo.dto.response;

import java.time.LocalDate;

import com.derabbit.seolstudy.domain.todo.Todo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoSummaryResponse {

    private Long id;
    private String title;
    private LocalDate date;
    private String subject;
    private Integer state;
    private Boolean isMine;
    /** 해당 과제에 멘티가 올린 파일(이미지 등) 개수 */
    private Long fileCount;

    public static TodoSummaryResponse from(Todo todo, Long userId) {
        return TodoSummaryResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .date(todo.getDate())
                .subject(todo.getSubject())
                .state(todo.getState())
                .isMine(todo.getCreator().getId().equals(userId))
                .fileCount(0L)
                .build();
    }

    public static TodoSummaryResponse from(TodoWithMine t) {
        Todo todo = t.todo();
        return TodoSummaryResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .date(todo.getDate())
                .subject(todo.getSubject())
                .state(todo.getState())
                .isMine(t.isMine())
                .fileCount(0L)
                .build();
    }

    public static TodoSummaryResponse from(TodoWithMine t, long fileCount) {
        Todo todo = t.todo();
        return TodoSummaryResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .date(todo.getDate())
                .subject(todo.getSubject())
                .state(todo.getState())
                .isMine(t.isMine())
                .fileCount(fileCount)
                .build();
    }
}
