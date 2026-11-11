// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 로그인 성공 시 돌려주는 상자입니다. 일부러 password는 넣지 않아서 비밀번호가 밖으로 나가지 않습니다.
public record LoginResponse(String loginId, String nickname) {
}
