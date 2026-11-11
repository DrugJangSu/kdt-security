// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 스프링 앱을 실행시키는 도구입니다.
import org.springframework.boot.SpringApplication;
// 스프링 부트의 기본 설정을 한 번에 켜 주는 어노테이션입니다.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 이 패키지 아래의 컴포넌트를 찾고, 의존성·설정에 맞는 Boot 자동설정을 적용합니다.
@SpringBootApplication
public class KdtSecurityApplication {
    // 자바 프로그램은 항상 main 메서드에서 시작합니다. 앱의 출발점입니다.
    // Spring 컨테이너와 내장 웹 서버를 시작합니다.
    public static void main(String[] args) {
        // main에 들어오면 컨테이너와 내장 웹 서버를 시작합니다. 기동이 끝난 뒤 HTTP 요청을 보냅니다.
        SpringApplication.run(KdtSecurityApplication.class, args);
    }
}
