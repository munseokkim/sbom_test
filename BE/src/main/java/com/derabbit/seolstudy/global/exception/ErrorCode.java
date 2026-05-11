package com.derabbit.seolstudy.global.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

    /*
     * =========================
     * 1000번대 : 공통 오류
     * =========================
     */
    INVALID_INPUT(1000, HttpStatus.BAD_REQUEST, "잘못된 입력입니다."),
    INVALID_DATE(1001, HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다."),
    DATA_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),

    /*
     * =========================
     * 2000번대 : 인증 / 인가
     * =========================
     */
    LOGIN_FAIL(2000, HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    AUTH_REQUIRED(2002, HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    ACCESS_DENIED(2003, HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    PASSWORD_MISMATCH(2004, HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),
    
    /*
     * =========================
     * 3000번대 : 멘토 기능
     * =========================
     */
    MENTEE_NOT_ASSIGNED(3000, HttpStatus.FORBIDDEN, "담당 멘티가 아닙니다."),
    TODO_NOT_FOUND(3001, HttpStatus.NOT_FOUND, "할 일을 찾을 수 없습니다."),
    FEEDBACK_NOT_FOUND(3002, HttpStatus.NOT_FOUND, "피드백이 존재하지 않습니다."),
    INVALID_TARGET_USER(3003, HttpStatus.BAD_REQUEST, "대상이 멘티가 아닙니다."),

    /*
     * =========================
     * 4000번대 : 멘티 기능
     * =========================
     */
    TODO_EDIT_FORBIDDEN(4000, HttpStatus.FORBIDDEN, "본인이 생성한 할 일만 수정할 수 있습니다."),
    TODO_DELETE_FORBIDDEN(4001, HttpStatus.FORBIDDEN, "본인이 생성한 할 일만 삭제할 수 있습니다."),
    STUDY_TIME_INVALID(4002, HttpStatus.BAD_REQUEST, "공부 시간 값이 올바르지 않습니다."),
    MENTOR_NOT_FOUND(4003, HttpStatus.NOT_FOUND, "담당 멘토를 찾을 수 없습니다"),

    /*
     * =========================
     * 5000번대 : 파일
     * =========================
     */
    FILE_UPLOAD_FAIL(5000, HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_TYPE_INVALID(5001, HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    FILE_NOT_FOUND(5002, HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    /*
     * =========================
     * 9000번대 : 서버
     * =========================
     */
    DB_ERROR(9000, HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),
    INTERNAL_ERROR(9001, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(int code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
