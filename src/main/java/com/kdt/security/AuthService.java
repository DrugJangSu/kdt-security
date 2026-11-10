package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 요청의 처리 순서와 DB 작업을 담당하는 Service를 Spring 빈으로 등록합니다.
@Service
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class AuthService {
    // MemberRepository로 회원을 조회하거나 저장합니다.
    private final MemberRepository memberRepository;
    // 비밀번호를 해시로 저장하거나, 저장된 해시로 입력 비밀번호가 맞는지 확인할 때 사용합니다.
    private final PasswordEncoder passwordEncoder;

    // 조회용 트랜잭션을 사용합니다. 회원 조회와 해시 비교를 이 메서드 안에서 마칩니다.
    @Transactional(readOnly = true)
    // 로그인 입력을 확인한 뒤 공개할 회원 정보만 반환합니다.
    public LoginResponse login(LoginRequest request) {
        // 1. 입력한 loginId로 회원을 조회합니다. 회원이 없으면 orElseThrow에서 LoginFailedException을 던집니다.
        Member member =
                this.memberRepository
                        .findByLoginId(request.loginId())
                        .orElseThrow(LoginFailedException::new);
        // 2. Member에서 DB에 저장된 비밀번호 해시를 읽습니다. 이 값과 입력한 비밀번호를 matches에 전달합니다.
        String stored = member.getPassword();
        // 해시가 없거나 입력의 UTF-8 길이가 BCrypt 제한을 넘으면 같은 로그인 실패로 거절합니다.
        if (stored == null
                || stored.isBlank()
                || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new LoginFailedException();
        }
        // 3. matches로 입력 비밀번호가 맞는지 확인합니다. false이면 ! 조건이 참이 되므로 로그인 실패 예외를 던집니다.
        // 이 줄에 중단점을 두면 정상·오류 요청이 어떤 분기로 나뉘는지 볼 수 있습니다.
        // Debug에서 관찰하려고 평문·해시를 출력하는 코드를 추가할 필요는 없습니다.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            throw new LoginFailedException();
        }
        // 4. 성공한 회원의 아이디·닉네임만 반환합니다. 저장 해시나 입력 비밀번호는 넣지 않습니다.
        return new LoginResponse(member.getLoginId(), member.getNickname());
    }
}