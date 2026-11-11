package com.kdt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 요청의 처리 순서와 DB 작업을 담당하는 Service를 Spring 빈으로 등록합니다.
@Service
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
@RequiredArgsConstructor
public class AuthService {
    // MemberRepository로 회원을 조회하거나 저장합니다.
    private final MemberRepository memberRepository;
    // 비밀번호를 해시로 저장하거나, 저장된 해시로 입력 비밀번호가 맞는지 확인할 때 사용합니다.
    private final PasswordEncoder passwordEncoder;

    // 조회용 트랜잭션을 사용합니다.
    @Transactional(readOnly = true)
    // 로그인 입력을 확인한 뒤 공개할 회원 정보만 반환합니다.
    public LoginResponse login(LoginRequest request) {
        // 1. 입력한 loginId로 회원을 조회합니다. 없으면 로그인 실패.
        Member member =
                this.memberRepository
                        .findByLoginId(request.loginId())
                        .orElseThrow(LoginFailedException::new);
        // 2. DB에 저장된 비밀번호 해시를 읽습니다.
        String stored = member.getPassword();
        // 해시가 없거나 입력이 72바이트를 넘으면 로그인 실패.
        if (stored == null
                || stored.isBlank()
                || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new LoginFailedException();
        }
        // 3. 입력 비밀번호가 저장된 해시와 맞는지 확인합니다. 틀리면 로그인 실패.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            throw new LoginFailedException();
        }
        // 4. 성공한 회원의 아이디·닉네임만 반환합니다.
        return new LoginResponse(member.getLoginId(), member.getNickname());
    }

    // 쓰기 트랜잭션입니다. 정상 종료되면 변경 내용이 DB에 반영됩니다.
    @Transactional
    // 확인된 회원 한 명의 API Key를 새 값으로 갱신합니다.
    public ApiKeyResponse issueApiKey(LoginRequest request) {
        // 1. 입력한 loginId로 회원을 조회합니다. 없으면 로그인 실패.
        Member member =
                this.memberRepository
                        .findByLoginId(request.loginId())
                        .orElseThrow(LoginFailedException::new);
        // 2. DB에 저장된 비밀번호 해시를 읽습니다.
        String stored = member.getPassword();
        // 해시가 없거나 입력이 72바이트를 넘으면 로그인 실패.
        if (stored == null
                || stored.isBlank()
                || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new LoginFailedException();
        }
        // 3. 입력 비밀번호가 저장된 해시와 맞는지 확인합니다. 틀리면 로그인 실패.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            throw new LoginFailedException();
        }
        // 4. 비밀번호 확인 후에만 36자 랜덤 키를 만듭니다.
        String apiKey = UUID.randomUUID().toString();
        // 5. 같은 회원의 키 칸을 새 값으로 덮어씁니다. 옛 키는 더 이상 못 씁니다.
        member.setApiKey(apiKey);
        // 6. 저장합니다. (@Transactional이라 더티 체킹으로도 반영되지만 명시적으로 저장)
        this.memberRepository.save(member);
        // 7. 발급한 키만 담아 돌려줍니다.
        return new ApiKeyResponse(apiKey);
    }
}