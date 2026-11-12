// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 토큰 안에 든 정보(payload) 묶음을 나타내는 타입입니다.
import io.jsonwebtoken.Claims;
// 토큰이 위조·만료·형식 오류일 때 JJWT가 던지는 예외의 부모 타입입니다.
import io.jsonwebtoken.JwtException;
// JWT를 만들고(builder) 읽는(parser) 진입점입니다.
import io.jsonwebtoken.Jwts;
// 문자열 바이트로 서명용 키를 만들어 주는 도구입니다.
import io.jsonwebtoken.security.Keys;
// 문자 인코딩(UTF-8) 상수를 쓰기 위해 가져옵니다.
import java.nio.charset.StandardCharsets;
// "지금 이 순간"의 시각을 나타내는 타입입니다.
import java.time.Instant;
// JJWT가 시각 값으로 받는 옛 방식의 날짜 타입입니다.
import java.util.Date;
// 서명에 쓰는 비밀 키의 타입입니다.
import javax.crypto.SecretKey;
// application.yml 같은 설정 값을 변수에 넣어 주는 어노테이션입니다.
import org.springframework.beans.factory.annotation.Value;
// 특별한 역할 없이 Spring이 관리하는 일반 부품으로 등록하는 어노테이션입니다.
import org.springframework.stereotype.Component;

// Spring이 이 클래스를 찾아 관리할 빈으로 등록합니다.
@Component
public class JwtService {

    // 토큰의 사용 목적을 access로 기록·확인할 때 쓰는 상수입니다.
    public static final String TYPE_ACCESS = "access";
    // 토큰의 사용 목적을 refresh로 기록·확인할 때 쓰는 상수입니다.
    public static final String TYPE_REFRESH = "refresh";
    // payload에서 토큰 용도를 저장한 항목 이름입니다.
    public static final String CLAIM_TYPE = "tokenType";

    // JWT를 서명·검증할 키 객체입니다. 입력 비밀번호나 토큰 문자열이 아닙니다.
    private final SecretKey key;
    // 설정에서 받은 access 수명입니다. 단위는 초입니다.
    private final long accessSeconds;
    // 설정에서 받은 refresh 수명입니다. 단위는 초입니다.
    private final long refreshSeconds;

    // 설정 문자열로 서명 키를 계산하므로 이 생성자는 직접 작성합니다.
    public JwtService(
            // application.yml의 해당 설정값을 이 생성자 인자로 주입받습니다.
            @Value("${kdt.jwt.secret}") String secret,
            // application.yml의 해당 설정값을 이 생성자 인자로 주입받습니다.
            @Value("${kdt.jwt.access-seconds}") long accessSeconds,
            // application.yml의 해당 설정값을 이 생성자 인자로 주입받습니다.
            @Value("${kdt.jwt.refresh-seconds}") long refreshSeconds) {
        // 문자열을 UTF-8 바이트로 바꾸어 실제 서명 키를 계산합니다. 너무 짧은 키는 기동 단계에서 실패합니다.
        // 앱 시작 때 계산해 둡니다. 실행 중 YAML만 고쳐도 이 객체의 키가 즉시 바뀌지는 않습니다.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // 주입받은 수명을 보관하고 발급 시 현재 시각에 초 단위로 더합니다.
        this.accessSeconds = accessSeconds;
        // refresh 수명도 같은 방식으로 보관합니다.
        this.refreshSeconds = refreshSeconds;
    }

    // access 용도와 짧은 수명으로 공통 발급기를 호출합니다.
    public String createAccess(String loginId) {
        // 공통 발급기에 access 용도와 access 수명을 넘깁니다.
        return compact(loginId, TYPE_ACCESS, this.accessSeconds);
    }

    // refresh 용도와 긴 수명으로 공통 발급기를 호출합니다.
    public String createRefresh(String loginId) {
        // 같은 사용자여도 refresh 용도·수명으로 별도 토큰을 만듭니다.
        return compact(loginId, TYPE_REFRESH, this.refreshSeconds);
    }

    // 서명·만료와 access 용도를 함께 확인합니다.
    public Claims parseAccess(String token) {
        // 서명만이 아니라 tokenType=access인지도 함께 확인하도록 요청합니다.
        return parseWithType(token, TYPE_ACCESS);
    }

    // 서명·만료와 refresh 용도를 함께 확인합니다.
    public Claims parseRefresh(String token) {
        // 재발급 입력이 tokenType=refresh인 경우에만 통과시킵니다.
        return parseWithType(token, TYPE_REFRESH);
    }

    // 헤더에서 Bearer 접두어를 제외한 토큰 값만 남깁니다.
    public String bearerValue(String authorization) {
        // 헤더 누락 또는 기대한 Bearer 형식이 아니면 토큰 값을 사용하지 않습니다.
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            // 형식이 틀렸으니 인증 실패(401) 예외를 던집니다.
            throw new TokenFailedException();
        }
        // Bearer 접두어를 자르고 토큰 앞뒤 공백을 제거합니다. 남은 값만 파서에 보냅니다.
        String token = authorization.substring("Bearer ".length()).trim();
        // 접두어만 있고 토큰이 없는 요청도 여기서 인증 실패로 거절합니다.
        if (token.isBlank()) {
            // 토큰이 비어 있으니 인증 실패(401) 예외를 던집니다.
            throw new TokenFailedException();
        }
        // 깨끗이 다듬어진 토큰 문자열만 돌려줍니다.
        return token;
    }

    // loginId·tokenType·발급 및 만료 시각·jti를 넣고 서명한 JWT 문자열을 만듭니다.
    private String compact(String loginId, String tokenType, long seconds) {
        // 이번 발급의 기준 시각입니다. 만료 시각은 이 값에 seconds를 더해 계산합니다.
        Instant now = Instant.now();
        // 빌더로 토큰의 내용을 한 줄씩 채워 나갑니다.
        return Jwts.builder()
                // sub에 이 토큰의 사용자 식별자를 담습니다. 비밀번호를 넣는 자리가 아닙니다.
                .subject(loginId)
                // jti에 발급마다 다른 식별자를 넣습니다. 이것만으로 옛 토큰이 폐기되지는 않습니다.
                .id(java.util.UUID.randomUUID().toString())
                // 사용자 정의 tokenType을 기록해 access와 refresh를 구분합니다.
                .claim(CLAIM_TYPE, tokenType)
                // iat는 발급 시각입니다. Date 변환은 JJWT가 받을 시간 타입에 맞춘 처리입니다.
                .issuedAt(Date.from(now))
                // exp는 만료 시각입니다. 파싱할 때 현재 시각이 이를 넘으면 거절됩니다.
                .expiration(Date.from(now.plusSeconds(seconds)))
                // 서명 키와 HS256으로 내용의 무결성을 확인할 서명을 만듭니다. payload를 암호화하는 과정은 아닙니다.
                .signWith(this.key, Jwts.SIG.HS256)
                // header.payload.signature 형태로 전송할 JWT 문자열을 최종 반환합니다.
                .compact();
    }

    // 클레임을 신뢰하기 전에 서명·만료·용도·필수 항목을 검증합니다.
    private Claims parseWithType(String token, String expectedType) {
        // 파서에 비어 있는 값을 넘기기 전에 직접 거절합니다.
        if (token == null || token.isBlank()) {
            // 토큰이 비어 있으니 인증 실패 예외를 던집니다.
            throw new TokenFailedException();
        }
        try {
            // 서명·만료를 검증한 뒤에만 클레임을 읽습니다. 단순 디코딩과 다른 지점입니다.
            // 키 설정 → 파서 생성 → 서명 검증하며 해석 → payload 꺼내기를 한 번에 처리합니다.
            Claims claims =
                    Jwts.parser().verifyWith(this.key).build().parseSignedClaims(token).getPayload();
            // 서버가 발급할 때 넣은 tokenType을 꺼냅니다. 아직 기대 용도와의 비교가 남아 있습니다.
            Object type = claims.get(CLAIM_TYPE);
            // 용도가 없거나 기대한 값과 다르면 거절합니다. null이면 오른쪽 toString 검사는 실행하지 않습니다.
            if (type == null || !expectedType.equals(type.toString())) {
                // 용도가 다르면(예: access 자리에 refresh) 인증 실패 예외를 던집니다.
                throw new TokenFailedException();
            }
            // 사용자 식별자와 만료 시각은 필수입니다. 서명이 맞아도 이 정보가 없는 토큰은 받지 않습니다.
            if (claims.getSubject() == null
                    || claims.getSubject().isBlank()
                    || claims.getExpiration() == null) {
                // 필수 항목이 빠졌으니 인증 실패 예외를 던집니다.
                throw new TokenFailedException();
            }
            // 검증을 모두 통과한 클레임만 호출한 서비스·필터에 넘깁니다.
            return claims;
        } catch (JwtException | IllegalArgumentException ex) {
            // 만료·서명·형식 오류를 모두 TokenFailedException으로 바꿉니다.
            throw new TokenFailedException();
        }
    }
}