// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// record는 생성자와 id()·title()·body()·memberId()·loginId() 접근자를 만들어 줍니다.
// 글 하나를 클라이언트에게 보여 줄 때 쓰는 상자입니다. DB 엔티티(Post)를 그대로 보내지 않고 이 상자에 옮겨 담습니다.
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
