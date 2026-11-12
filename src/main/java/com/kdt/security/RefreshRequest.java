// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 값이 null이거나 빈 문자열·공백뿐이면 검사에서 걸러 주는 어노테이션입니다.
import jakarta.validation.constraints.NotBlank;

// record는 생성자와 refreshToken() 접근자를 만들어 줍니다.
public record RefreshRequest(
        // 재발급할 때 제시하는 refresh 값입니다. 앞에 Bearer를 붙이지 않습니다.
        // null·공백 문자열을 검사합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotBlank String refreshToken
) {}