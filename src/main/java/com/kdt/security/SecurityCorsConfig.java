package com.kdt.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

// Spring이 이 클래스의 설정을 읽어 적용합니다.
@Configuration
public class SecurityCorsConfig implements WebMvcConfigurer {

    @Override
    // 다른 Origin의 화면이 사용할 경로·메서드·헤더를 선언합니다.
    public void addCorsMappings(CorsRegistry registry) {
        // /api/** 경로의 브라우저 CORS 규칙입니다.
        registry.addMapping("/api/**")
                // 허용할 화면 Origin입니다.
                .allowedOrigins("http://127.0.0.1:8000")
                // 브라우저가 요청할 수 있는 메서드입니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 브라우저가 보낼 수 있는 헤더입니다.
                .allowedHeaders("Content-Type", "Authorization", "X-Api-Key");
    }
}