package com.kdt.security;

// 조회한 대상이 없을 때 사용하는 예외입니다. 메시지를 받아 404 응답 처리에 전달합니다.
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}