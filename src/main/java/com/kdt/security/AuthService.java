// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;
// 비밀번호 암호화·비교 도구의 공통 규격입니다. 실제로는 BCrypt가 들어옵니다.
import org.springframework.security.crypto.password.PasswordEncoder;
// 이 클래스를 "서비스"로 Spring에 등록하는 어노테이션입니다.
import org.springframework.stereotype.Service;
// DB 작업을 한 묶음(트랜잭션)으로 처리하게 하는 어노테이션입니다.
import org.springframework.transaction.annotation.Transactional;

// 겹칠 가능성이 거의 없는 랜덤 문자열(UUID)을 만드는 도구입니다. API 키를 만들 때 씁니다.
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
    // JWT 만들기·검사하기를 맡는 기계입니다.
    private final JwtService jwtService;

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
        // (BCrypt는 72바이트까지만 비교하므로, 너무 긴 입력은 미리 막습니다.)
        if (stored == null
                || stored.isBlank()
                || request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            // 위 조건 중 하나라도 맞으면 로그인 실패 예외를 던져 즉시 끝냅니다.
            throw new LoginFailedException();
        }
        // 3. 입력 비밀번호가 저장된 해시와 맞는지 확인합니다. 틀리면 로그인 실패.
        // matches(입력, 해시): 입력을 같은 방식으로 암호화해 해시와 맞는지 비교합니다. 해시를 풀어서 비교하는 게 아닙니다.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            // 비밀번호가 틀렸으니 로그인 실패 예외를 던집니다.
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
            // 조건에 걸리면 로그인 실패 예외를 던져 즉시 끝냅니다.
            throw new LoginFailedException();
        }
        // 3. 입력 비밀번호가 저장된 해시와 맞는지 확인합니다. 틀리면 로그인 실패.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            // 비밀번호가 틀렸으니 로그인 실패 예외를 던집니다.
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

    // ===================== 여기부터 03차시 추가 =====================

    // 조회용 트랜잭션입니다. 비번 확인만 하고 DB는 바꾸지 않습니다.
    @Transactional(readOnly = true)
    // 비밀번호가 맞으면 access·refresh 토큰 2개를 발급합니다.
    public TokenResponse issueTokens(LoginRequest request) {
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
            // 조건에 걸리면 로그인 실패 예외를 던져 즉시 끝냅니다.
            throw new LoginFailedException();
        }
        // 3. 입력 비밀번호가 저장된 해시와 맞는지 확인합니다. 틀리면 로그인 실패.
        if (!this.passwordEncoder.matches(request.password(), stored)) {
            // 비밀번호가 틀렸으니 로그인 실패 예외를 던집니다.
            throw new LoginFailedException();
        }
        // 4. 15분짜리 access 토큰을 만듭니다. 안에 loginId가 적힙니다.
        String access = this.jwtService.createAccess(member.getLoginId());
        // 5. 7일짜리 refresh 토큰을 따로 만듭니다.
        String refresh = this.jwtService.createRefresh(member.getLoginId());
        // 6. 두 토큰을 상자에 담아 돌려줍니다.
        return new TokenResponse(access, refresh);
    }

    // "나 누구야?" — Bearer access 토큰을 검사하고 안에 적힌 loginId를 돌려줍니다.
    public MeResponse me(String authorization) {
        // 1. "Bearer " 떼고 토큰만 꺼냅니다. 헤더가 없으면 여기서 401.
        String token = this.jwtService.bearerValue(authorization);
        // 2. 도장·만료·access 용도를 검사합니다. 하나라도 틀리면 401.
        io.jsonwebtoken.Claims claims = this.jwtService.parseAccess(token);
        // 3. 토큰 안의 sub(loginId)를 읽어 돌려줍니다. DB는 조회하지 않습니다.
        return new MeResponse(claims.getSubject());
    }

    // refresh 토큰을 검사하고, 통과하면 새 access·refresh를 발급합니다.
    public TokenResponse refresh(RefreshRequest request) {
        // 1. 도장·만료·refresh 용도를 검사합니다. access를 넣으면 여기서 401.
        io.jsonwebtoken.Claims claims = this.jwtService.parseRefresh(request.refreshToken());
        // 2. 토큰 안의 loginId를 꺼냅니다.
        String loginId = claims.getSubject();
        // 3. 같은 loginId로 새 토큰 2개를 만들어 돌려줍니다. (옛 refresh가 자동으로 막히진 않음)
        return new TokenResponse(
                this.jwtService.createAccess(loginId),
                this.jwtService.createRefresh(loginId));
    }
}