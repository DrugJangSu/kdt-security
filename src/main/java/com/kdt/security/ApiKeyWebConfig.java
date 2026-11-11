// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;
// 이 클래스가 설정용 클래스임을 알리는 어노테이션입니다.
import org.springframework.context.annotation.Configuration;
// "어느 경로에 어떤 인터셉터를 세울지" 등록할 때 쓰는 도구입니다.
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// 이 인터페이스를 구현하면 웹(MVC) 동작을 우리 입맛대로 바꿀 수 있습니다.
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Spring이 이 클래스의 설정을 읽어 적용합니다.
@Configuration
// final 필드를 받는 생성자를 Lombok이 만들고, Spring이 인터셉터를 넣어 줍니다.
@RequiredArgsConstructor
// CORS 설정(SecurityCorsConfig)과 역할을 나눠서, 이 클래스는 "경비원 배치"만 맡습니다.
public class ApiKeyWebConfig implements WebMvcConfigurer {

    // 출입증을 검사하는 경비원입니다.
    private final ApiKeyInterceptor apiKeyInterceptor;

    // 부모 인터페이스(WebMvcConfigurer)의 메서드를 우리 방식으로 구현한다는 표시입니다.
    @Override
    // 경비원을 어느 문 앞에 세울지 등록합니다.
    // 등록하지 않으면 인터셉터는 만들어만 두고 실제로는 실행되지 않습니다.
    public void addInterceptors(InterceptorRegistry registry) {
        // 글 API(/api/v1/posts/**)에만 경비원을 세웁니다.
        // /api/v1/auth/** (로그인·API 키 발급)는 경비원을 거치지 않습니다. 키를 받기 전에 키를 요구하면 안 되기 때문입니다.
        // 경비원은 GET·POST는 그냥 통과시키므로 실제 검사는 PUT·DELETE(글 수정·삭제)에만 적용됩니다.
        registry.addInterceptor(this.apiKeyInterceptor).addPathPatterns("/api/v1/posts/**");
    }
}
