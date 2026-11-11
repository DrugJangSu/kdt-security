// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 조회한 대상이 없을 때 사용하는 예외입니다. 메시지를 받아 404 응답 처리에 전달합니다.
public class NotFoundException extends RuntimeException {
    // 예외를 만들 때 적은 문장("post missing" 등)을 부모 클래스에 넘겨 보관합니다.
    public NotFoundException(String message) { super(message); }
}
