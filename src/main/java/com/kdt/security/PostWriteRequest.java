package com.kdt.security;

import jakarta.validation.constraints.*;

// record는 생성자와 title()·body() 접근자를 만들어 줍니다.
public record PostWriteRequest(
        // 작성·수정할 제목입니다. 작성자 번호는 이 DTO로 받지 않습니다.
        // null·공백 문자열을 검사하고 문자열 길이를 200 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotBlank @Size(max = 200) String title,
        // 작성·수정할 글 본문입니다.
        // null·공백 문자열을 검사하고 문자열 길이를 10000 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotBlank @Size(max = 10000) String body
) {}