// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// @NotNull, @NotBlank, @Size 같은 입력 검증 규칙을 한꺼번에 가져옵니다.
import jakarta.validation.constraints.*;

// record는 생성자와 memberId()·title()·body() 접근자를 만들어 줍니다.
// 글을 새로 쓸 때 클라이언트가 보내는 값 세 개를 담는 상자입니다.
public record PostCreateRequest(
        // 현재 중간 단계에서 입력으로 받는 작성자 번호입니다. 실제 회원 존재는 서비스가 확인합니다.
        // 값 누락을 검사합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotNull Long memberId,
        // 저장할 글 제목입니다.
        // null·공백 문자열을 검사하고 문자열 길이를 200 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotBlank @Size(max = 200) String title,
        // 저장할 글 본문입니다.
        // null·공백 문자열을 검사하고 문자열 길이를 10000 이하로 제한합니다. HTTP 요청에서는 Controller의 @Valid가 검사를 실행합니다.
        @NotBlank @Size(max = 10000) String body
) {
}
