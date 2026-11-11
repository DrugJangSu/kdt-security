// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// "누구인지는 알지만 이 일을 할 권한이 없다"를 알리는 예외입니다. 예: 남의 글을 고치려 할 때(403).
// 401(누군지 모름)과 다르다는 점을 기억하세요.
public class ForbiddenException extends RuntimeException {
}
