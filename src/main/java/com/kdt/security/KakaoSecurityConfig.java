package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class KakaoSecurityConfig {

    private final KakaoOAuthSuccessHandler successHandler;
    private final KakaoOAuthFailureHandler failureHandler;

    @Bean
    @Order(1)
    public SecurityFilterChain kakaoFilterChain(HttpSecurity http) throws Exception {

        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(Customizer.withDefaults())
                .oauth2Login(oauth -> oauth.successHandler((this.successHandler)).failureHandler(this.failureHandler));
        return http.build();

    }
}
