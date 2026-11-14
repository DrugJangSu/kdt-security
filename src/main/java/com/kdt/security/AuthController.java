package com.kdt.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "auth")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final CookieAuthWriter cookieAuthWriter;

    @Operation(summary = "Login with loginId and password")
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return this.authService.login(request);
    }

    @Operation(summary = "Issue API key")
    @PostMapping("/api-key")
    public ApiKeyResponse issueApiKey(@Valid @RequestBody LoginRequest request) {
        return this.authService.issueApiKey(request);
    }

    @Operation(summary = "Issue access and refresh tokens")
    @PostMapping("/token")
    public TokenResponse token(@Valid @RequestBody LoginRequest request) {
        return this.authService.issueTokens(request);
    }

    @Operation(summary = "Reissue tokens with refresh token")
    @PostMapping("/token/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return this.authService.refresh(request);
    }

    @PostMapping("/token-cookie")
    public AccessTokenResponse tokenCookie(
            @Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokens = this.authService.issueTokens(request);
        this.cookieAuthWriter.writeRefresh(response, tokens.refreshToken());
        this.cookieAuthWriter.writeDemoVisible(response);
        return new AccessTokenResponse(tokens.accessToken());
    }

    @PostMapping("/token/refresh-cookie")
    public AccessTokenResponse refreshCookie(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new TokenFailedException();
        }
        TokenResponse tokens = this.authService.refresh(new RefreshRequest(refreshToken));
        this.cookieAuthWriter.writeRefresh(response, tokens.refreshToken());
        return new AccessTokenResponse(tokens.accessToken());
    }

    @GetMapping("/me")
    public MeResponse me(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        return this.authService.me(authorization);
    }

    @Operation(summary = "Delete refresh cookie")
    @PostMapping("/logout-cookie")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logoutCookie(HttpServletResponse response) {
        this.cookieAuthWriter.clearRefresh(response);
    }
}