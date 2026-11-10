package com.kdt.security;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

// MVC 처리 중 지정한 예외를 공통 HTTP 오류 응답으로 바꾸는 클래스입니다.
@RestControllerAdvice
public class ApiExceptionAdvice {
    // MVC 요청 처리 중 NotFoundException이 발생하면 아래 메서드로 404 응답을 만듭니다.
    @ExceptionHandler(NotFoundException.class)
    // 존재하지 않는 회원·글을 조회한 요청에 404를 반환합니다.
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        // 지정한 HTTP 오류 상태와 ApiError 본문을 함께 반환합니다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }

    // MVC 요청 처리 중 MethodArgumentNotValidException이 발생하면 아래 메서드로 오류 응답을 만듭니다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 요청 DTO 검증 실패를 400 응답으로 바꿉니다.
    public ResponseEntity<ApiError> handleInvalid(MethodArgumentNotValidException ex) {
        // 지정한 HTTP 오류 상태와 ApiError 본문을 함께 반환합니다.
        return ResponseEntity.badRequest()
                             .body(new ApiError("INVALID_INPUT", "invalid input"));
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<ApiError> loginFailed(LoginFailedException ex) {
        return ResponseEntity.status(401).body(new ApiError("LOGIN_FAILED", "login failed"));
    }
}