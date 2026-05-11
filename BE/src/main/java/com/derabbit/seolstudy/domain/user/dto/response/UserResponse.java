package com.derabbit.seolstudy.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String email;
    private String name;
    private String school;
    private Long grade;
    private String role;
}