package com.derabbit.seolstudy.domain.user.dto.request;

import lombok.Getter;

@Getter
public class LoginRequest {

    private String email;
    private String password;
    private String role;

}