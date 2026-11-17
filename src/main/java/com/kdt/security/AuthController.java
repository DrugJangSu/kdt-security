package com.kdt.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Swagger 문서에 표시할 이름과 설명입니다. 요청의 인증·인가 규칙은 별도로 작성합니다.
@Tag(name = "auth")
// 이 클래스의 메서드가 반환한 값을 HTTP 응답 본문으로 보냅니다.
@RestController
// 각 메서드의 경로 앞에 붙일 공통 URL 경로입니다.
@RequestMapping("/api/v1/auth")
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class AuthController {
    // 회원 확인과 토큰 발급·재발급 처리를 AuthService에 요청합니다.
    private final AuthService authService;
    // 발급한 refresh를 Set-Cookie 응답 헤더로 옮깁니다.
    private final CookieAuthWriter cookieAuthWriter;

    @Operation(summary = "Login with loginId and password")
    // POST /api/v1/auth/login 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/login")
    // 로그인 JSON을 검증하고 서비스 결과를 응답합니다.
    // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        // 검증한 DTO를 Service에 전달합니다. 반환한 DTO는 JSON으로 보내고, 등록된 예외는 예외 처리기가 오류 응답으로 바꿉니다.
        return this.authService.login(request);
    }

    @Operation(summary = "Issue API key")
    // POST /api/v1/auth/api-key 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/api-key")
    // 키 발급 요청을 서비스에 전달합니다.
    // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
    public ApiKeyResponse issueApiKey(@Valid @RequestBody LoginRequest request) {
        // 키 발급은 서비스가 담당합니다. Controller가 회원을 조회하거나 비밀번호를 직접 비교하지 않습니다.
        return this.authService.issueApiKey(request);
    }

    @Operation(summary = "Issue access and refresh tokens")
    // POST /api/v1/auth/token 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/token")
    // 토큰 발급 요청을 서비스에 전달합니다.
    // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
    public TokenResponse token(@Valid @RequestBody LoginRequest request) {
        // 비밀번호 확인과 두 JWT 생성이 끝난 결과를 JSON으로 반환합니다.
        return this.authService.issueTokens(request);
    }

    @Operation(summary = "Reissue tokens with refresh token")
    // POST /api/v1/auth/token/refresh 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/token/refresh")
    // JSON 본문으로 받은 refresh를 검증·재발급 서비스에 전달합니다.
    // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        // JSON으로 받은 refresh를 재발급 서비스에 전달합니다. 쿠키 기반 경로와 입력을 섞지 않습니다.
        return this.authService.refresh(request);
    }

    // POST /api/v1/auth/token-cookie 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/token-cookie")
    // 발급 결과의 refresh는 쿠키에, access는 JSON에 담습니다.
    public AccessTokenResponse tokenCookie(
            // JSON을 요청 DTO로 바꾸고 @NotBlank·@Size 등의 입력 검증을 수행합니다.
            // Spring이 전달한 응답 객체입니다. 이 객체에 응답 헤더와 상태 코드를 작성합니다.
            @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        // 먼저 비밀번호를 확인하고 토큰을 발급합니다. 예외가 나면 아래 쿠키 작성 줄에는 도달하지 않습니다.
        TokenResponse tokens = this.authService.issueTokens(request);
        // 내부 결과의 refresh는 JSON 대신 Set-Cookie 응답 헤더에 넣습니다.
        this.cookieAuthWriter.writeRefresh(response, tokens.refreshToken());
        // HttpOnly와 Path 설정을 비교할 쿠키도 추가합니다. 이 쿠키로 사용자를 인증하지는 않습니다.
        this.cookieAuthWriter.writeDemoVisible(response);
        // JSON에는 access만 공개합니다. TokenResponse 전체를 반환하면 refresh도 노출되므로 구분합니다.
        return new AccessTokenResponse(tokens.accessToken());
    }

    // POST /api/v1/auth/token/refresh-cookie 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping("/token/refresh-cookie")
    // 쿠키에서 refresh를 읽고 재발급한 쿠키와 access로 응답합니다.
    public AccessTokenResponse refreshCookie(
            // 요청 쿠키의 refreshToken 값을 받습니다. required=false라서 누락되면 null로 직접 검사합니다.
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            // Spring이 전달한 응답 객체입니다. 이 객체에 응답 헤더와 상태 코드를 작성합니다.
            HttpServletResponse response) {
        // required=false로 받은 누락·빈 쿠키를 이 API의 401 TOKEN_FAILED로 변환합니다.
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenFailedException();
        }
        // 쿠키에서 읽은 값을 서비스 입력 DTO로 감쌉니다. 서비스는 토큰 검증에 집중합니다.
        TokenResponse tokens = this.authService.refresh(new RefreshRequest(refreshToken));
        // 내부 결과의 refresh는 JSON 대신 Set-Cookie 응답 헤더에 넣습니다.
        this.cookieAuthWriter.writeRefresh(response, tokens.refreshToken());
        // JSON에는 access만 공개합니다. TokenResponse 전체를 반환하면 refresh도 노출되므로 구분합니다.
        return new AccessTokenResponse(tokens.accessToken());
    }

    // GET /api/v1/auth/me 요청을 바로 아래 메서드에 연결합니다.
    @GetMapping("/me")
    // 이번 차시에서 정한 인증 정보로 현재 사용자 응답을 만듭니다.
    // loginId는 JWT 필터가 인증한 principal입니다. 요청 JSON의 아이디를 신뢰해 넣지 않습니다.
    public MeResponse me(@AuthenticationPrincipal String loginId) {
        // 현재 인증 객체에서 기대한 문자열 principal을 못 받은 경우 정상 사용자 응답을 만들지 않습니다.
        if (loginId == null || loginId.isBlank()) {
            throw new TokenFailedException();
        }
        // 필터가 검증한 이름만 응답합니다. 여기서 비밀번호나 Authorization을 다시 읽지 않습니다.
        return new MeResponse(loginId);
    }
    // refresh 쿠키를 지우는 API입니다. JSON 요청 본문은 받지 않습니다.
    // CookieOriginConfig가 허용된 Origin인지 먼저 검사합니다.
    @Operation(summary = "Delete refresh cookie")
    @PostMapping("/logout-cookie")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 쿠키 삭제 헤더만 보내고 본문은 비웁니다.
    public void logoutCookie(HttpServletResponse response) {
        this.cookieAuthWriter.clearRefresh(response);
    }
}