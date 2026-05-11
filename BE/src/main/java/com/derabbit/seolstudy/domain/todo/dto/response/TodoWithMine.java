package com.derabbit.seolstudy.domain.todo.dto.response;

import com.derabbit.seolstudy.domain.todo.Todo;

public record TodoWithMine(
    Todo todo,
    boolean isMine
) {}