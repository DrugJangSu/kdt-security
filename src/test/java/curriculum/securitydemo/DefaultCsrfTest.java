package curriculum.securitydemo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// 지정한 예제 설정만 올립니다. 본문 앱의 DB·보안 설정 전체를 바꾸지 않습니다.
@SpringJUnitWebConfig(DefaultCsrfTest.DemoConfig.class)
class DefaultCsrfTest {
    // 이 테스트에서 준비한 웹 컨텍스트를 주입받습니다.
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    // 각 테스트를 실행하기 전에 MockMvc 객체를 다시 준비합니다.
    @BeforeEach
    // MockMvc가 Security 필터도 거쳐 요청을 처리하도록 연결합니다.
    void setup() {
        // MockMvc에 Security 필터를 연결해 접근 규칙과 CSRF 검사를 함께 확인합니다.
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    // @Test가 붙은 메서드 하나가 테스트 한 개입니다.
    @Test
    // 기본 CSRF 보호가 토큰 없는 POST를 거절하는지 확인합니다.
    void missingTokenIs403() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        mvc.perform(post("/demo/notes")).andExpect(status().isForbidden());
    }

    @Test
        // 유효한 테스트용 CSRF 토큰을 보낸 POST가 Controller까지 진행하는지 확인합니다.
    void validTokenIs200() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        // 테스트 요청 헤더에 유효한 CSRF 토큰을 추가합니다.
        mvc.perform(post("/demo/notes").with(csrf().asHeader()))
                .andExpect(status().isOk())
                .andExpect(content().string("saved"));
    }

    // Spring이 이 클래스의 설정과 @Bean 메서드를 읽어 적용합니다.
    @Configuration
    // 이 컨텍스트에서 Servlet 보안 설정과 SecurityFilterChain을 사용할 수 있게 합니다.
    @EnableWebSecurity
    // 이 예제 컨텍스트에서 Controller 매핑 등 Spring MVC 기능을 사용할 수 있게 합니다.
    @EnableWebMvc
    static class DemoConfig {
        // 반환한 객체를 Spring 빈으로 등록합니다. 필요한 클래스가 이 빈을 주입받아 사용합니다.
        @Bean
        // 인가만 공개하며 기본 CSRF 보호는 유지합니다.
        // Spring이 전달한 보안 설정 빌더입니다. 규칙을 모아 아래 build()로 체인을 만듭니다.
        SecurityFilterChain demoChain(HttpSecurity http) throws Exception {
            // 인가 조건은 공개지만 CSRF를 disable하지 않았으므로 토큰 없는 POST는 거절됩니다.
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        // 반환한 객체를 Spring 빈으로 등록합니다. 필요한 클래스가 이 빈을 주입받아 사용합니다.
        @Bean
        // 이 테스트에서 사용할 Controller를 등록합니다.
        NoteController noteController() {
            return new NoteController();
        }
    }

    // 이 클래스의 메서드가 반환한 값을 HTTP 응답 본문으로 보냅니다.
    @RestController
    static class NoteController {
        // POST /demo/notes 요청을 바로 아래 메서드에 연결합니다.
        @PostMapping("/demo/notes")
        // Controller에 도달했다는 확인용 문자열을 반환합니다.
        String save() {
            // 여기까지 도달했음을 확인하는 문자열입니다. 이 예제는 DB를 저장하지 않습니다.
            return "saved";
        }
    }
}