package com.kdt.security;

import jakarta.validation.constraints.*;

// record는 생성자와 title()·body() 접근자를 만들어 줍니다.
public record PostUpdateRequest(
    // 기존 글의 제목을 이 값으로 변경합니다.
    // null·공백 문자열을 검사하고 문자열 길이를 200 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
    @NotBlank @Size(max = 200) String title,
    // 기존 글의 본문을 이 값으로 변경합니다.
    // null·공백 문자열을 검사하고 문자열 길이를 10000 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
    @NotBlank @Size(max = 10000) String body) {}
