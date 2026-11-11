// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// "API 키가 없거나 틀렸다"를 알리는 예외입니다. 즉 누구인지 확인하지 못한 상황(401)에 씁니다.
// RuntimeException을 상속해서 throw만 하면 되고 따로 선언하지 않아도 됩니다.
public class ApiKeyFailedException extends RuntimeException {
}
