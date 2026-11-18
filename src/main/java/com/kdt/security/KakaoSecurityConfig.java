package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Spring이 이 클래스의 설정과 @Bean 메서드를 읽어 적용합니다.
@Configuration
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class KakaoSecurityConfig {
    // 카카오 인증 성공 뒤 로컬 JWT 응답을 만들 핸들러입니다.
    private final KakaoOAuthSuccessHandler successHandler;
    // 카카오 인증 실패를 고정된 오류 응답으로 바꿀 핸들러입니다.
    private final KakaoOAuthFailureHandler failureHandler;

    // 반환한 객체를 Spring 빈으로 등록합니다. 필요한 클래스가 이 빈을 주입받아 사용합니다.
    @Bean
    // 숫자가 작은 체인부터 요청 경로를 검사합니다. 처음 맞는 체인 하나만 실행합니다.
    @Order(1)
    // OAuth 시작·콜백에만 사용할 별도 필터 체인을 만듭니다.
    // Spring이 전달한 보안 설정 빌더입니다. 규칙을 모아 아래 build()로 체인을 만듭니다.
    // 매 요청이 아니라 앱 시작 때 구성합니다. 요청마다 처음 일치한 체인 하나를 선택해 실행합니다.
    public SecurityFilterChain kakaoFilterChain(HttpSecurity http) throws Exception {
        // OAuth 시작·콜백 경로일 때만 이 체인을 선택합니다. 나머지 API는 기존 체인이 맡습니다.
        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                // 선택된 이 체인 안에서 접근 허용 여부를 정합니다. anyRequest는 체인의 모든 요청입니다.
                // 이 체인이 맡는 OAuth 시작·콜백은 로그인 전에도 들어와야 합니다.
                // permitAll이어도 oauth2Login의 code·state 검증은 실행됩니다. 나머지 API에는 이 규칙이 적용되지 않습니다.
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 카카오 인가 요청·state를 임시 세션에 둘 수 있게 합니다. API 체인의 정책은 바꾸지 않습니다.
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // 이 예제 체인의 기본 CSRF 정책은 유지합니다.
                .csrf(Customizer.withDefaults())
                // 카카오 설정으로 로그인 흐름을 구성하고 성공·실패 핸들러를 연결합니다.
                .oauth2Login(oauth -> oauth.successHandler(successHandler).failureHandler(failureHandler));
        // 설정한 규칙을 실제 SecurityFilterChain 객체로 완성해 빈으로 반환합니다.
        return http.build();
    }
}