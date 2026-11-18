package com.kdt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

// Spring이 이 클래스를 찾아 관리할 빈으로 등록합니다.
@Component
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class KakaoOAuthSuccessHandler implements AuthenticationSuccessHandler {

    // 외부 사용자 식별자를 기존 또는 새 로컬 회원으로 연결합니다.
    private final OAuthAccountService oauthAccountService;
    // JWT 발급과 서명·만료·용도 검증을 담당합니다.
    private final JwtService jwtService;

    @Override
    // 외부 사용자 정보를 로컬 회원과 JWT로 연결합니다.
    public void onAuthenticationSuccess(
            // request는 현재 요청과 세션을 읽고, response는 상태·헤더·본문을 작성합니다.
            // authentication은 Spring이 확인한 외부 사용자 인증 결과입니다.
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        // 이 핸들러는 oauth2Login에만 연결했으므로 OAuth2 인증 결과로 해석합니다.
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        // user-name-attribute=id에 따른 외부 회원번호입니다. 아직 우리 loginId가 아닙니다.
        String kakaoId = oauth.getName();
        // 외부 식별자가 없으면 회원 저장·JWT 발급을 진행하지 않습니다.
        if (kakaoId == null || kakaoId.isBlank()) {
            // 유효하지 않은 외부 사용자 정보로 400을 반환하고 종료합니다.
            response.sendError(400);
            return;
        }
        Member member;
        try {
            // 외부 식별자로 기존 연결을 찾거나 생성한 뒤 로컬 회원을 받습니다.
            member = this.oauthAccountService.link("kakao", kakaoId, nickname(oauth));
        } catch (RuntimeException ex) {
            // 로컬 회원 연결 처리 실패입니다. 외부 비밀 응답 대신 고정 오류를 반환합니다.
            // 기존 임시 세션만 가져옵니다. 정리를 위해 새 세션을 만들지 않습니다.
            var session = request.getSession(false);
            if (session != null) {
                // 서버의 임시 세션을 정리합니다. 외부 토큰·이미 발급한 JWT의 폐기와는 다릅니다.
                session.invalidate();
            }
            // 외부 인증과 구분되는 로컬 연결 실패 상태입니다.
            response.setStatus(500);
            // 직접 작성할 응답 문자열의 인코딩을 Writer 사용 전에 정합니다.
            response.setCharacterEncoding("UTF-8");
            // Controller의 자동 직렬화가 아닌 직접 JSON 응답임을 알립니다.
            response.setContentType("application/json");
            response
                    .getWriter()
                    .write("{\"code\":\"OAUTH_LINK_FAILED\",\"message\":\"oauth link failed\"}");
            return;
        }
        // 연결된 로컬 loginId로 우리 JWT를 만듭니다. 카카오 access_token을 그대로 반환하지 않습니다.
        // 이 로컬 토큰을 05-security.http의 kakaoAccess에 복사합니다. 카카오 access_token과 구분합니다.
        String access = this.jwtService.createAccess(member.getLoginId());
        // 기존 임시 세션만 가져옵니다. 정리를 위해 새 세션을 만들지 않습니다.
        var session = request.getSession(false);
        if (session != null) {
            // 서버의 임시 세션을 정리합니다. 외부 토큰·이미 발급한 JWT의 폐기와는 다릅니다.
            session.invalidate();
        }
        // 민감한 발급 응답을 일반 응답처럼 캐시 저장하지 않도록 지시합니다.
        response.setHeader("Cache-Control", "no-store");
        // 이후 본문은 새 로컬 accessToken 하나를 가진 성공 응답입니다.
        response.setStatus(200);
        // 직접 작성할 응답 문자열의 인코딩을 Writer 사용 전에 정합니다.
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Controller의 자동 직렬화가 아닌 직접 JSON 응답임을 알립니다.
        response.setContentType("application/json");
        // 발급한 access 문자열 한 항목만 JSON에 씁니다. 비밀번호·Member 전체는 넣지 않습니다.
        response.getWriter().write("{\"accessToken\":\"" + access + "\"}");
    }

    // 중첩 사용자 정보에서 닉네임을 읽고 없으면 기본값을 반환합니다.
    private static String nickname(OAuth2AuthenticationToken oauth) {
        // 사용자 정보의 kakao_account 항목을 읽습니다. 항목이 없을 수 있어 Object로 확인합니다.
        Object account = oauth.getPrincipal().getAttributes().get("kakao_account");
        // Map이면 accountMap으로 읽고, 아니면 기본 닉네임을 반환합니다.
        if (!(account instanceof java.util.Map<?, ?> accountMap)) {
            return "kakao";
        }
        // 그 안의 profile 객체로 한 단계 더 들어갑니다.
        Object profile = accountMap.get("profile");
        // profile도 Map인지 확인한 뒤에만 nickname을 읽습니다.
        if (!(profile instanceof java.util.Map<?, ?> profileMap)) {
            return "kakao";
        }
        // 표시 이름만 꺼냅니다. 외부 사용자의 식별자는 위의 kakaoId입니다.
        Object name = profileMap.get("nickname");
        // 동의하지 않았거나 값이 비면 기본 이름 kakao로 처리합니다.
        if (name == null || name.toString().isBlank()) {
            return "kakao";
        }
        // 확인한 표시 이름을 문자열로 반환합니다.
        return name.toString();
    }
}