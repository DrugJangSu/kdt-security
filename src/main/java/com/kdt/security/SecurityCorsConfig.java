package com.kdt.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

// Spring이 이 클래스의 설정과 @Bean 메서드를 읽어 적용합니다.
@Configuration
public class SecurityCorsConfig implements WebMvcConfigurer {
    @Override
    // 다른 Origin의 화면이 사용할 경로·메서드·헤더를 선언합니다.
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 URL이 아니라 /api/**의 브라우저 CORS 규칙을 구성합니다.
        registry.addMapping("/api/**")
                // 허용할 화면 Origin입니다. 같은 호스트라도 포트가 다르면 다른 Origin입니다.
                .allowedOrigins("http://127.0.0.1:8000")
                // 브라우저가 요청할 수 있는 메서드 목록입니다. 실제 쓰기의 인증·인가 검사는 따로 남습니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 브라우저가 전송할 사용자 정의 헤더를 허용합니다. 헤더의 토큰 값까지 검증하지는 않습니다.
                .allowedHeaders("Content-Type", "Authorization", "X-Api-Key");
    }
}