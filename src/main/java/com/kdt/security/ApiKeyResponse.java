// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// API 키 발급 성공 시 돌려주는 상자입니다. 새로 만든 apiKey 문자열 하나만 담습니다.
public record ApiKeyResponse(String apiKey) {}
