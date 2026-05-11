package com.derabbit.seolstudy.global.exception;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.derabbit.seolstudy.global.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 만든 비즈니스 예외 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // 날짜 파라미터 형식 오류 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        if (isDateParameter(e)) {
            return ResponseEntity
                    .status(ErrorCode.INVALID_DATE.getHttpStatus())
                    .body(ErrorResponse.of(ErrorCode.INVALID_DATE));
        }

        return ResponseEntity
                .status(ErrorCode.INVALID_INPUT.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT));
    }

    // 예상 못한 서버 에러
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 원인 파악을 위해 실제 예외 스택트레이스를 로그로 남김
        log.error("Unhandled exception", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR));
    }

    private boolean isDateParameter(MethodArgumentTypeMismatchException e) {
        if ("date".equals(e.getName())) {
            return true;
        }
        Class<?> requiredType = e.getRequiredType();
        return requiredType != null && LocalDate.class.isAssignableFrom(requiredType);
    }
}
