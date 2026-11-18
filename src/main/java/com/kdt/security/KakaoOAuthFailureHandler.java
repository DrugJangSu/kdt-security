package com.kdt.security;

import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

// Spring이 이 클래스를 찾아 관리할 빈으로 등록합니다.
@Component
public class KakaoOAuthFailureHandler implements AuthenticationFailureHandler {
    @Override
    // OAuth 인증 실패를 고정된 JSON과 상태 코드로 응답합니다.
    public void onAuthenticationFailure(
            // request는 현재 요청과 세션을 읽고, response는 상태·헤더·본문을 작성합니다.
            // exception은 Spring이 전달한 인증 실패 예외입니다.
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        // 실패한 로그인 왕복의 기존 세션이 있는 경우만 가져옵니다.
        HttpSession session = request.getSession(false);
        if (session != null) {
            // 임시 상태를 정리하고 다음 로그인은 새 시작 주소에서 진행하게 합니다.
            session.invalidate();
        }
        // 인증 흐름 실패를 401로 응답합니다. DB 연결 저장 실패 500과 다른 단계입니다.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 오류 문자열을 작성하기 전에 UTF-8을 지정합니다.
        response.setCharacterEncoding("UTF-8");
        // 응답 본문이 JSON임을 지정합니다.
        response.setContentType("application/json");
        response
                .getWriter()
                // 예외 원문 대신 고정된 OAUTH_LOGIN_FAILED 메시지만 작성합니다.
                .write("{\"code\":\"OAUTH_LOGIN_FAILED\",\"message\":\"oauth login failed\"}");
    }
}