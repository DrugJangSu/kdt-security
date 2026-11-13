package com.kdt.security;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// Controller에서 함께 사용할 쿠키 헤더 작성 도구를 빈으로 등록합니다.
@Component
public class CookieAuthWriter {

    // 브라우저에 전달할 실제 refresh 쿠키 이름입니다.
    public static final String REFRESH = "refreshToken";
    // 인증에 쓰지 않는 관찰용 쿠키 이름입니다.
    public static final String DEMO = "demoVisible";

    // refresh를 HttpOnly 쿠키로 전달할 응답 헤더를 추가합니다.
    // response에 Set-Cookie 헤더를 더하며, HTTP Client에서는 Headers에서 이 설정을 확인합니다.
    // 실제 저장 여부는 브라우저에서 따로 봅니다.
    public void writeRefresh(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie =
                // 쿠키 이름과 값을 정해 응답용 쿠키 설정을 만듭니다. 아직 브라우저에 전송된 상태는 아닙니다.
                ResponseCookie.from(REFRESH, refreshToken)
                        // document.cookie로 값이 읽히지 않게 합니다. 브라우저의 자동 전송까지 막는 속성은 아닙니다.
                        .httpOnly(true)
                        // 로컬 HTTP 실습 설정입니다. 실제 HTTPS 환경에서는 전송 정책에 맞춰 true를 사용합니다.
                        .secure(false)
                        // auth 경로와 그 하위 경로의 요청에 보내도록 범위를 좁힙니다. 글 목록 경로와는 다릅니다.
                        .path("/api/v1/auth")
                        // 사이트 간 요청에서 쿠키를 보내는 조건을 제한합니다. Origin 검증을 대신하는 속성은 아닙니다.
                        .sameSite("Lax")
                        // 쿠키 수명을 7일로 맞춥니다. 토큰 안의 exp는 별도이므로 refresh 설정과 함께 조정합니다.
                        .maxAge(Duration.ofSeconds(604800))
                        .build();
        // Set-Cookie 한 줄을 응답에 추가합니다. addHeader로 앞에서 작성한 다른 쿠키도 유지합니다.
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 스크립트에서 읽을 수 있는 관찰용 쿠키를 추가합니다. response에 Set-Cookie 헤더를 더합니다.
    public void writeDemoVisible(HttpServletResponse response) {
        ResponseCookie cookie =
                // 쿠키 이름과 값을 정해 응답용 쿠키 설정을 만듭니다. 아직 브라우저에 전송된 상태는 아닙니다.
                ResponseCookie.from(DEMO, "kimbap")
                        // 비교용 쿠키는 스크립트에서 읽을 수 있게 둡니다. 인증에 사용하는 값이 아닙니다.
                        .httpOnly(false)
                        // 로컬 HTTP 실습 설정입니다. 실제 HTTPS 환경에서는 전송 정책에 맞춰 true를 사용합니다.
                        .secure(false)
                        // 이 비교 쿠키는 같은 호스트의 모든 경로에서 관찰할 수 있게 합니다.
                        .path("/")
                        // 사이트 간 요청에서 쿠키를 보내는 조건을 제한합니다. Origin 검증을 대신하는 속성은 아닙니다.
                        .sameSite("Lax")
                        // 비교·독립 예제 쿠키의 보관 시간은 600초입니다.
                        .maxAge(Duration.ofSeconds(600))
                        .build();
        // Set-Cookie 한 줄을 응답에 추가합니다. addHeader로 앞에서 작성한 다른 쿠키도 유지합니다.
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
    // refresh 쿠키와 같은 이름·Path로 만료 응답을 보냅니다.
    // 이 메서드는 쿠키 저장소를 정리할 뿐, 이미 발급한 JWT를 서버에서 취소하지 않습니다.
    public void clearRefresh(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH, "")
                .httpOnly(true)
                .secure(false) // 앞의 발급 설정과 같은 로컬 HTTP 조건입니다.
                .path("/api/v1/auth") // 발급 때의 Path와 같아야 같은 쿠키를 지웁니다.
                .sameSite("Lax")
                .maxAge(Duration.ZERO) // 0초로 만료시킵니다. 음수로 설정하지 않습니다.
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}