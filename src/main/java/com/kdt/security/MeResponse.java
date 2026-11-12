// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// record는 생성자와 loginId() 접근자를 만들어 줍니다.
public record MeResponse(
        // 검증된 JWT의 sub에서 읽은 사용자 이름입니다. 회원 DB 재조회 결과가 아닙니다.
        String loginId
) {}