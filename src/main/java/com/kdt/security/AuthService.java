package com.kdt.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.LoginException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public LoginResponse Login(LoginRequest request) {

        Member member = this.memberRepository.findByLoginId(request.loginId()).orElseThrow(LoginFailedException::new);
        String stored = member.getPassword();

        if (stored == null                    // 저장된 비번이 없거나
                || stored.isBlank()               // 비어 있거나
                || request.password().getBytes(StandardCharsets.UTF_8).length > 72) {   // 입력이 너무 길면(72바이트 이상)
            throw new LoginFailedException(); // → 실패
        }
    }
}
