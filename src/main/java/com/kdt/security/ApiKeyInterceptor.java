// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 들어온 HTTP 요청의 정보(메서드, 헤더 등)를 담은 객체입니다.
import jakarta.servlet.http.HttpServletRequest;
// 서버가 돌려줄 HTTP 응답을 담은 객체입니다.
import jakarta.servlet.http.HttpServletResponse;
// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;
// Spring이 이 클래스를 찾아 관리하게 하는 어노테이션입니다.
import org.springframework.stereotype.Component;
// 컨트롤러가 실행되기 전·후에 끼어들 수 있게 해 주는 규격(인터페이스)입니다.
import org.springframework.web.servlet.HandlerInterceptor;

// "문지기"입니다. 컨트롤러가 실행되기 전에 먼저 요청을 가로채서 출입증(API 키)을 확인합니다.
// Spring이 이 클래스를 관리할 빈으로 등록합니다.
@Component
// final 필드(memberRepository, apiCaller)를 받는 생성자를 자동으로 만들어, Spring이 필요한 객체를 넣어 줍니다.
@RequiredArgsConstructor
public class ApiKeyInterceptor implements HandlerInterceptor {

    // 읽을 헤더 이름
    // 클라이언트는 요청 헤더에 "X-Api-Key: YOUR_API_KEY" 형태로 출입증을 보냅니다.
    public static final String HEADER = "X-Api-Key";

    // 명단 (출입증으로 회원 찾기)
    // API 키로 어느 회원인지 DB에서 찾을 때 씁니다.
    private final MemberRepository memberRepository;
    // 손님 쪽지
    // 확인된 회원을 이번 요청 동안 기록해 두는 곳입니다. 이후 컨트롤러·서비스가 꺼내 봅니다.
    private final ApiCaller apiCaller;

    // 부모 인터페이스(HandlerInterceptor)의 메서드를 우리 방식으로 구현한다는 표시입니다.
    @Override
    // 컨트롤러 실행 직전에 호출됩니다. true를 돌려주면 계속 진행하고, 예외를 던지면 거기서 멈춥니다.
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // ① GET·POST·OPTIONS·HEAD는 출입증 검사 없이 통과
        // 이번 요청이 GET인지 PUT인지 같은 HTTP 메서드 이름을 꺼냅니다.
        String method = request.getMethod();
        // equalsIgnoreCase는 대소문자를 구분하지 않고 같은지 비교합니다.
        if ("GET".equalsIgnoreCase(method)
                || "POST".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)) {
            // 검사 없이 컨트롤러로 보냅니다. 즉 검사 대상은 PUT·DELETE뿐입니다(글 수정·삭제).
            return true;
        }

        // ② 출입증 꺼내기. 없거나 빈칸이면 401
        // 요청 헤더에서 X-Api-Key 값을 읽습니다. 헤더가 없으면 null이 나옵니다.
        String key = request.getHeader(HEADER);
        // 키가 없거나 공백뿐이면 출입증을 안 가져온 것입니다.
        if (key == null || key.isBlank()) {
            // 401 처리용 예외를 던져 여기서 요청을 멈춥니다.
            throw new ApiKeyFailedException();
        }

        // ③ 명단에서 출입증 주인 찾기. 없으면 401
        // 받은 키와 같은 apiKey를 가진 회원을 DB에서 찾습니다.
        Member member = this.memberRepository.findByApiKey(key)
                // 그런 회원이 없으면 가짜·만료된 키이므로 401 예외를 던집니다.
                .orElseThrow(ApiKeyFailedException::new);

        // ④ 쪽지에 적고 통과
        // 누구인지 확인된 회원을 쪽지(ApiCaller)에 적어 둡니다.
        this.apiCaller.setMember(member);
        // 검사를 통과했으니 컨트롤러로 계속 진행합니다.
        return true;
    }
}
