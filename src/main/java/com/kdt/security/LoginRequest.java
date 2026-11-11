// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 값이 null이거나 빈 문자열("")이거나 공백만 있으면 안 된다는 규칙을 쓰기 위해 가져옵니다.
import jakarta.validation.constraints.NotBlank;

// 로그인 요청으로 받는 값 두 개를 담는 상자입니다.
// @NotBlank: 비어 있으면 컨트롤러의 @Valid가 400(INVALID_INPUT)으로 거절합니다.
public record LoginRequest (@NotBlank String loginId, @NotBlank String password) {

}
