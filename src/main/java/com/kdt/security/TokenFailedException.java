// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 토큰 검증에 실패했을 때 던지는 예외입니다. RuntimeException을 상속하면 throws를 따로 적지 않아도 됩니다.
// 내용이 비어 있어도 "TokenFailedException이 발생했다"는 사실만으로 ApiExceptionAdvice가 401로 바꿔 줍니다.
public class TokenFailedException extends RuntimeException {}
