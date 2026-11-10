package com.kdt.security;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class AuthPasswordSeeder implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        this.memberRepository.findByLoginId("kdt01")
                .ifPresent(member -> {
            if (member.getPassword() == null || member.getPassword() .isBlank()) {
                member.setPassword(this.passwordEncoder.encode("kimbap12"));
                this.memberRepository.save(member);
            }
        });
    }
}
