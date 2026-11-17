package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PasswordAuthenticationService {

    private final AuthenticationManager authenticationManager;

    public String authenticateLoginId(LoginRequest request) {

        if (request.loginId() == null
                || request.loginId().isBlank()
                || request.password() == null
                || request.password().isBlank()
                || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new LoginFailedException();
        }

        Authentication input =
                UsernamePasswordAuthenticationToken.unauthenticated(request.loginId(), request.password());

        try {
            return authenticationManager.authenticate(input).getName();

        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            throw new LoginFailedException();
        }
    }
}