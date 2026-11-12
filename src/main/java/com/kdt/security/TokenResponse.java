// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// record는 생성자와 accessToken()·refreshToken() 접근자를 만들어 줍니다.
public record TokenResponse(
        // 보호 API의 Authorization: Bearer에 넣는 토큰입니다.
        String accessToken,
        // 재발급에 사용할 토큰입니다. 쿠키 API는 이 값을 JSON에 다시 노출하지 않습니다.
        String refreshToken
) {}