package com.derabbit.seolstudy.domain.todo.dto.request;

import java.time.LocalDate;

import lombok.Getter;

@Getter
public class MentorUpdateTodoRequest {

    private String title;
    private LocalDate date;
    private String subject;
    private String goal;
}

