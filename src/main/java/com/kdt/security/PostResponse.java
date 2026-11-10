package com.kdt.security;

// record는 생성자와 id()·title()·body()·memberId()·loginId() 접근자를 만들어 줍니다.
public record PostResponse(
        // 방금 저장하거나 조회한 글 번호입니다.
        Long id,
        // 응답으로 공개할 제목입니다.
        String title,
        // 응답으로 공개할 글 본문입니다.
        String body,
        // 실제 글 작성자의 로컬 회원 번호입니다.
        Long memberId,
        // 실제 작성자의 로그인 아이디입니다. 비밀번호는 포함하지 않습니다.
        String loginId
) {
}