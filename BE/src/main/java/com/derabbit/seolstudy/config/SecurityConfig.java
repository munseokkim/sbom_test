package com.derabbit.seolstudy.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import com.derabbit.seolstudy.global.exception.ErrorCode;
import com.derabbit.seolstudy.global.jwt.JwtFilter;
import com.derabbit.seolstudy.global.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://seolstudy.kr",
                "https://www.seolstudy.kr"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {

        http.cors(cors -> {}).csrf(csrf -> csrf.disable());

        AccessDeniedHandlerImpl defaultAccessDenied = new AccessDeniedHandlerImpl();
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String header = request.getHeader("Authorization");
                    if (header != null && header.startsWith("Bearer ")) {
                        writeError(response, ErrorCode.ACCESS_DENIED);
                        return;
                    }
                    writeError(response, ErrorCode.AUTH_REQUIRED);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                        writeError(response, ErrorCode.AUTH_REQUIRED);
                        return;
                    }
                    defaultAccessDenied.handle(request, response, accessDeniedException);
                })
        );

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/mentor/**").hasRole("MENTOR")
                .requestMatchers("/api/mentee/**").hasRole("MENTEE")
                .requestMatchers("/api/files/*/download").permitAll()
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        try {
            String body = "{\"code\":" + errorCode.getCode() + ",\"message\":\"" + errorCode.getMessage() + "\"}";
            response.getWriter().write(body);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
