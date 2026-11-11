// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 이 클래스가 설정용 클래스임을 알리는 어노테이션입니다.
import org.springframework.context.annotation.Configuration;
// CORS(다른 주소의 화면이 이 서버를 호출해도 되는지) 규칙을 설정하는 도구들을 가져옵니다.
import org.springframework.web.servlet.config.annotation.*;

// Spring이 이 클래스의 설정을 읽어 적용합니다.
@Configuration
// WebMvcConfigurer를 구현하면 웹(MVC) 동작을 우리 입맛대로 바꿀 수 있습니다.
// 인터셉터(경비원) 등록은 ApiKeyWebConfig가 따로 맡고, 이 클래스는 CORS만 담당합니다.
public class SecurityCorsConfig implements WebMvcConfigurer {

    // 부모 인터페이스의 메서드를 우리 방식으로 다시 정의한다는 표시입니다.
    @Override
    // 다른 Origin의 화면이 사용할 경로·메서드·헤더를 선언합니다.
    public void addCorsMappings(CorsRegistry registry) {
        // /api/** 경로의 브라우저 CORS 규칙입니다.
        registry.addMapping("/api/**")
                // 허용할 화면 Origin입니다. 같은 호스트라도 포트가 다르면 다른 Origin입니다.
                .allowedOrigins("http://127.0.0.1:8000")
                // 브라우저가 요청할 수 있는 메서드입니다. CORS 허용이 인증·인가 검사를 대신하지는 않습니다.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                // 브라우저가 보낼 수 있는 헤더입니다. X-Api-Key는 API 키 출입증을 담는 헤더입니다.
                .allowedHeaders("Content-Type", "Authorization", "X-Api-Key");
    }
}