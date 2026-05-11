package com.derabbit.seolstudy.domain.auth;

// import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.derabbit.seolstudy.domain.user.User;
import com.derabbit.seolstudy.domain.user.dto.request.LoginRequest;
import com.derabbit.seolstudy.domain.user.dto.response.LoginResponse;
import com.derabbit.seolstudy.domain.user.repository.UserRepository;
import com.derabbit.seolstudy.global.exception.CustomException;
import com.derabbit.seolstudy.global.exception.ErrorCode;
import com.derabbit.seolstudy.global.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAIL));

        if(!request.getRole().equals(user.getRole().toString())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        // 인코딩후 비교 (배포용)
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new CustomException(ErrorCode.LOGIN_FAIL);
        // }

        // 평문 비교 (테스트용)
        if (!request.getPassword().equals(user.getPassword())) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }

        String token = jwtUtil.createToken(user.getId(), request.getRole());

        return LoginResponse.builder()
                .accessToken(token)
                .role(user.getRole().toString())
                .build();
    }
}