package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Spring이 이 클래스를 찾아 관리할 빈으로 등록합니다.
@Component
// 회원 생성 Runner(@Order(1))가 끝난 다음에 비밀번호 해시를 채웁니다.
@Order(2)
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class AuthPasswordSeeder implements ApplicationRunner {

    // MemberRepository로 회원을 조회하거나 저장합니다.
    private final MemberRepository memberRepository;
    // 비밀번호를 해시로 저장하거나, 저장된 해시로 입력 비밀번호가 맞는지 확인할 때 사용합니다.
    private final PasswordEncoder passwordEncoder;

    @Override
    // 회원 시드가 끝난 뒤 비어 있는 kdt01 비밀번호에만 해시를 저장합니다.
    public void run(ApplicationArguments args) {
        this.memberRepository
                // 앞 Runner가 준비한 회원을 찾습니다. 없으면 ifPresent 안은 실행하지 않습니다.
                .findByLoginId("kdt01")
                .ifPresent(
                        member -> {
                            // 비밀번호가 아직 없는 경우만 채웁니다. 이미 등록된 해시를 재시작 때 덮어쓰지 않습니다.
                            // IDE에서 앱을 다시 Run해도 이미 해시가 있는 회원은 이 조건을 통과하지 않습니다.
                            if (member.getPassword() == null || member.getPassword().isBlank()) {
                                // 평문 kimbap12를 바로 저장하지 않고 BCrypt 결과 문자열을 넣습니다.
                                member.setPassword(this.passwordEncoder.encode("kimbap12"));
                                // 조회한 객체의 변경값을 명시적으로 저장합니다. 필드만 바꾸고 끝내지 않습니다.
                                this.memberRepository.save(member);
                            }
                        });
    }
}