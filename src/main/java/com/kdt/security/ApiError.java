// 이 파일이 들어 있는 폴더(패키지) 이름입니다. 모든 파일이 같은 패키지에 있습니다.
package com.kdt.security;

// record는 생성자와 code()·message() 접근자를 만들어 줍니다.
// 쉽게 말해 "값 두 개를 담는 상자"입니다. 오류가 났을 때 클라이언트에게 돌려줄 모양을 정합니다.
public record ApiError(
        // HTTP 상태와 별개로 실패 종류를 구분하는 문자열입니다.
        String code,
        // 클라이언트에 공개할 오류 설명입니다.
        String message
) {
}
