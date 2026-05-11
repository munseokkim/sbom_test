package com.derabbit.seolstudy.domain.user.dto.response;

import com.derabbit.seolstudy.domain.user.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MenteeResponse {

    private Long id;
    private String name;
    private String school;
    private Long grade;

    public static MenteeResponse from(User user) {
        return new MenteeResponse(
                user.getId(),
                user.getName(),
                user.getSchool(),
                user.getGrade()
        );
    }
}
