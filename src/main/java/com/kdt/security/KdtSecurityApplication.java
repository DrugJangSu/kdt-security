package com.kdt.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 패키지 아래의 컴포넌트를 찾고, 의존성·설정에 맞는 Boot 자동설정을 적용합니다.
@SpringBootApplication
public class KdtSecurityApplication {
    // Spring 컨테이너와 내장 웹 서버를 시작합니다.
    public static void main(String[] args) {
        // main에 들어오면 컨테이너와 내장 웹 서버를 시작합니다. 기동이 끝난 뒤 HTTP 요청을 보냅니다.
        SpringApplication.run(KdtSecurityApplication.class, args);
    }
}