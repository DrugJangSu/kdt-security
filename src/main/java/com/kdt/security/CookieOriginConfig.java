package com.kdt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Set;

@Configuration
public class CookieOriginConfig implements WebMvcConfigurer {

    // 쿠키 POST를 허용할 출처 명단
    private static final Set<String> ALLOWED = Set.of("http://127.0.0.1:8080",
            "http://localhost:8080", "http://127.0.0.1:8000");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(
                new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        // 1. POST가 아니면 검사 없이 통과
                        if (!"POST".equalsIgnoreCase(request.getMethod())) {
                            return true;
                        }
                        // 2. 어디서 왔는지(Origin) 꺼내기
                        String origin = request.getHeader("Origin");
                        // 3. 없거나 명단에 없으면 403
                        if (origin == null || !ALLOWED.contains(origin)) {
                            throw new ForbiddenException();
                        }
                        // 4. 명단에 있으면 통과
                        return true;
                    }
                }
        ).addPathPatterns(
                "/api/v1/auth/token-cookie",
                "/api/v1/auth/token/refresh-cookie",
                "/api/v1/auth/logout-cookie");
    }
}