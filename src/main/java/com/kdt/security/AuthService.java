package com.kdt.security;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    // 비밀번호 확인을 AuthenticationManager 경로에 위임하는 서비스입니다.
    private final PasswordAuthenticationService passwordAuthenticationService;
    // JWT 발급과 서명·만료·용도 검증을 담당합니다.
    private final JwtService jwtService;

    // 조회용 트랜잭션을 사용합니다. 연관 객체를 읽어 응답 DTO로 바꾸는 작업도 이 메서드 안에서 마칩니다.
    @Transactional(readOnly = true)
    // 로그인 입력을 확인한 뒤 공개할 회원 정보만 반환합니다.
    public LoginResponse login(LoginRequest request) {
        // 1. 비밀번호 인증을 위임하고 성공한 사용자 이름만 받습니다.
        String loginId = passwordAuthenticationService.authenticateLoginId(request);
        // 2. 인증된 이름으로 회원을 읽습니다. 응답의 nickname이나 키 변경에 쓸 정보입니다.
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(LoginFailedException::new);
        // 3. 성공한 회원의 아이디·닉네임만 반환합니다. 저장 해시나 입력 비밀번호는 넣지 않습니다.
        return new LoginResponse(member.getLoginId(), member.getNickname());
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 확인된 회원 한 명의 API Key를 새 값으로 갱신합니다.
    public ApiKeyResponse issueApiKey(LoginRequest request) {
        // 1. 비밀번호 인증을 위임하고 성공한 사용자 이름만 받습니다.
        String loginId = passwordAuthenticationService.authenticateLoginId(request);
        // 2. 인증된 이름으로 회원을 읽습니다. 응답의 nickname이나 키 변경에 쓸 정보입니다.
        Member member = memberRepository.findByLoginId(loginId).orElseThrow(LoginFailedException::new);
        // 비밀번호가 확인된 뒤에만 36자 UUID를 생성합니다. 새 회원을 만드는 코드가 아닙니다.
        String key = UUID.randomUUID().toString();
        // 같은 회원 행의 현재 키 값을 덮어씁니다. 이후 옛 키로 조회하면 회원을 찾지 못합니다.
        member.setApiKey(key);
        // 발급한 키만 응답 DTO로 반환합니다. 키를 사용하는 요청의 권한 검사는 다음 요청에서 수행합니다.
        return new ApiKeyResponse(key);
    }

    // 비밀번호 인증이 끝난 loginId로 access와 refresh를 발급합니다.
    public TokenResponse issueTokens(LoginRequest request) {
        // 1. 비밀번호 인증을 위임하고 성공한 사용자 이름만 받습니다.
        String loginId = passwordAuthenticationService.authenticateLoginId(request);
        // 응답할 access·refresh를 묶습니다. 새 쌍을 반환해도 기존 refresh가 자동 폐기되지는 않습니다.
        return new TokenResponse(jwtService.createAccess(loginId), jwtService.createRefresh(loginId));
    }

    // 리프레시 토큰을 검증한 뒤 sub의 loginId로 새 액세스·리프레시 토큰을 발급합니다.
    public TokenResponse refresh(RefreshRequest request) {
        // refresh 검증이 끝난 뒤 sub를 읽습니다. 사용자가 본문으로 보낸 별도 아이디는 사용하지 않습니다.
        String loginId = jwtService.parseRefresh(request.refreshToken()).getSubject();
        // 응답할 access·refresh를 묶습니다. 새 쌍을 반환해도 기존 refresh가 자동 폐기되지는 않습니다.
        return new TokenResponse(jwtService.createAccess(loginId), jwtService.createRefresh(loginId));
    }
}