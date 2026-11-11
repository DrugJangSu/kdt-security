// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// @NotBlank, @Size 같은 입력 검증 규칙을 한꺼번에 가져옵니다.
import jakarta.validation.constraints.*;

// record는 생성자와 title()·body() 접근자를 만들어 줍니다.
// 글을 고칠 때 클라이언트가 보내는 새 제목과 새 본문을 담는 상자입니다. (작성자는 바꿀 수 없어서 없습니다)
public record PostUpdateRequest(
    // 기존 글의 제목을 이 값으로 변경합니다.
    // null·공백 문자열을 검사하고 문자열 길이를 200 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
    @NotBlank @Size(max = 200) String title,
    // 기존 글의 본문을 이 값으로 변경합니다.
    // null·공백 문자열을 검사하고 문자열 길이를 10000 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
    @NotBlank @Size(max = 10000) String body) {}
