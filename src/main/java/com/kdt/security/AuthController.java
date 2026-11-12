// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// Swagger 문서에서 각 API의 설명(summary)을 적을 때 쓰는 어노테이션입니다.
import io.swagger.v3.oas.annotations.Operation;
// Swagger 문서에서 API를 이름별로 묶어 보여 줄 때 쓰는 어노테이션입니다.
import io.swagger.v3.oas.annotations.tags.Tag;
// 요청으로 받은 값이 규칙(@NotBlank 등)을 지키는지 검사하게 해 주는 어노테이션입니다.
import jakarta.validation.Valid;
// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;
// @RestController, @RequestMapping, @PostMapping, @GetMapping, @RequestBody, @RequestHeader를 한꺼번에 가져옵니다. (*는 "전부"라는 뜻)
import org.springframework.web.bind.annotation.*;

// 이 클래스가 HTTP 요청을 받아 JSON으로 응답하는 "문" 역할임을 Spring에 알립니다.
@RestController
// Swagger 문서에서 이 컨트롤러를 "auth"라는 이름으로 묶어 보여 줍니다.
@Tag(name = "auth")
// 이 클래스의 모든 API 주소 앞에 /api/v1/auth 를 붙입니다.
@RequestMapping("/api/v1/auth")
// final 필드(authService)를 받는 생성자를 자동으로 만들어, Spring이 필요한 객체를 넣어 줍니다.
@RequiredArgsConstructor
public class AuthController {
    // 로그인과 API 키 발급의 실제 처리를 맡길 서비스입니다.
    private final AuthService authService;

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Login with loginId and password")
    // POST /api/v1/auth/login 요청을 아래 메서드에 연결합니다.
    @PostMapping("/login")
    // 요청 JSON을 LoginRequest로 바꾸고 @Valid로 빈 값이 아닌지 검사한 뒤 호출됩니다.
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        // 실제 로그인 판단은 서비스에 맡기고, 결과(아이디·닉네임)를 그대로 응답합니다.
        return this.authService.login(request);
    }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Issue API key")
    // POST /api/v1/auth/api-key 요청을 아래 메서드에 연결합니다.
    @PostMapping("/api-key")
    // 아이디·비밀번호를 다시 확인한 뒤 API 키를 발급합니다.
    public ApiKeyResponse issueApiKey(@Valid @RequestBody LoginRequest request) {
        // 비밀번호 확인과 키 발급은 서비스가 하고, 발급된 키를 응답합니다.
        return this.authService.issueApiKey(request);
    }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Issue access and refresh tokens")
    // POST /api/v1/auth/token 요청을 아래 메서드에 연결합니다.
    @PostMapping("/token")
    // 아이디·비밀번호가 맞으면 access 토큰과 refresh 토큰을 함께 발급합니다.
    public TokenResponse token(@Valid @RequestBody LoginRequest request) {
        // 비밀번호 확인과 토큰 발급은 서비스에 맡기고, 만들어진 토큰 2개를 응답합니다.
        return this.authService.issueTokens(request);
    }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Reissue tokens with refresh token")
    // POST /api/v1/auth/token/refresh 요청을 아래 메서드에 연결합니다.
    @PostMapping("/token/refresh")
    // 요청 JSON의 refreshToken이 비어 있지 않은지 검사한 뒤 호출됩니다.
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        // refresh 토큰 검사와 새 토큰 발급은 서비스에 맡기고 결과를 응답합니다.
        return this.authService.refresh(request);
    }

    // GET /api/v1/auth/me 요청을 아래 메서드에 연결합니다. (조회라서 GET을 씁니다)
    @GetMapping("/me")
    // 요청 헤더 Authorization 값을 문자열로 받습니다. required=false라서 헤더가 없어도 여기서 에러가 나지 않고 null이 들어옵니다.
    public MeResponse me(@RequestHeader(name="Authorization", required = false) String authorization) {
        // 헤더 검사(없으면 401)와 토큰 해석은 서비스에 맡기고, 내 loginId를 응답합니다.
        return this.authService.me(authorization);
    }

}
