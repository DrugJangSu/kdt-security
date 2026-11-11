package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring이 이 클래스의 설정을 읽어 적용합니다.
@Configuration
// final 필드를 받는 생성자를 Lombok이 만들고, Spring이 인터셉터를 넣어 줍니다.
@RequiredArgsConstructor
public class ApiKeyWebConfig implements WebMvcConfigurer {

    // 출입증을 검사하는 경비원입니다.
    private final ApiKeyInterceptor apiKeyInterceptor;

    @Override
    // 경비원을 어느 문 앞에 세울지 등록합니다.
    public void addInterceptors(InterceptorRegistry registry) {
        // 글 API(/api/v1/posts/**)에만 경비원을 세웁니다.
        registry.addInterceptor(this.apiKeyInterceptor).addPathPatterns("/api/v1/posts/**");
    }
}