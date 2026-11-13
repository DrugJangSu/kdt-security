package com.kdt.security;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieAuthWriter {

    public static final String REFRESH = "refreshToken";
    public static final String DEMO = "demoVisible";

    public void writeRefresh(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(604800))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 관찰용 쿠키: JS가 읽을 수 있게 (HttpOnly 끔)
    public void writeDemoVisible(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(DEMO, "kimbap")
                .httpOnly(false)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofSeconds(600))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // refresh 쿠키 삭제: 같은 이름·Path로 Max-Age=0
    public void clearRefresh(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH, "")
                .httpOnly(true)
                .secure(false)
                .path("/api/v1/auth")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}