package com.kdt.security;

// record는 생성자와 code()·message() 접근자를 만들어 줍니다.
public record ApiError(
        // HTTP 상태와 별개로 실패 종류를 구분하는 문자열입니다.
        String code,
        // 클라이언트에 공개할 오류 설명입니다.
        String message
) {
}