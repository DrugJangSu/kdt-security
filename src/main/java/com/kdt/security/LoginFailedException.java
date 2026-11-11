// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// "로그인 실패"를 알리는 예외입니다. RuntimeException을 상속해서 throw만 하면 되고 따로 선언하지 않아도 됩니다.
// 던져지면 ApiExceptionAdvice가 받아서 401 응답으로 바꿉니다.
public class LoginFailedException extends RuntimeException {
}
