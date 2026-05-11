package com.derabbit.seolstudy.domain.todo.dto.response;

import java.time.LocalDate;
import java.util.List;

import com.derabbit.seolstudy.domain.todo.Todo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoDetailResponse {

    private Long id;
    private Long menteeId;
    private Long creatorId;
    private String title;
    private LocalDate date;
    private String subject;
    private String goal;
    private Integer state;
    private String comment;
    private List<TodoFileResponse> files;

    public static TodoDetailResponse from(Todo todo, List<TodoFileResponse> files) {
        return TodoDetailResponse.builder()
                .id(todo.getId())
                .menteeId(todo.getMentee() == null ? null : todo.getMentee().getId())
                .creatorId(todo.getCreator() == null ? null : todo.getCreator().getId())
                .title(todo.getTitle())
                .date(todo.getDate())
                .subject(todo.getSubject())
                .goal(todo.getGoal())
                .state(todo.getState())
                .comment(todo.getComment())
                .files(files)
                .build();
    }
}
