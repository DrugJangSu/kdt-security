package curriculum.securitydemo;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// 이 테스트 안의 설정·Controller만 사용합니다. 본문의 JWT 필터나 회원 DB는 로드하지 않습니다.
@SpringJUnitWebConfig(RoleAuthorizationTest.DemoConfig.class)
class RoleAuthorizationTest {
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach
    void setup() {
        // 테스트 요청에도 Security 필터를 연결해야 hasRole 규칙이 실행됩니다.
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
        // 인증 정보가 없으면 EntryPoint가 401을 만듭니다.
    void anonymousIs401() throws Exception {
        mvc.perform(get("/demo/roles/staff")).andExpect(status().isUnauthorized());
    }

    @Test
    // 실제 DB 회원이 아니라 테스트용 인증 객체를 준비합니다. roles는 ROLE_ 접두어 없이 적습니다.
    @WithMockUser(username = "user01", roles = "USER")
    void userIs403() throws Exception {
        mvc.perform(get("/demo/roles/staff")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff01", roles = "STAFF")
    void staffIs200() throws Exception {
        mvc.perform(get("/demo/roles/staff"))
                .andExpect(status().isOk())
                .andExpect(content().string("staff only"));
    }

    @Configuration
    @EnableWebSecurity
    @EnableWebMvc
    static class DemoConfig {
        @Bean
        SecurityFilterChain roleChain(HttpSecurity http) throws Exception {
            http
                    // 이 GET 한 개에만 STAFF 역할을 요구합니다.
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.GET, "/demo/roles/staff").hasRole("STAFF")
                            .anyRequest().denyAll())
                    // 미인증 요청을 HTML 로그인 화면 대신 401로 확인합니다.
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(
                            (request, response, error) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED)))
                    .formLogin(form -> form.disable())
                    .httpBasic(basic -> basic.disable())
                    .logout(logout -> logout.disable());
            // GET만 사용하므로 기본 CSRF 설정은 바꾸지 않습니다.
            return http.build();
        }

        @Bean
        StaffController staffController() {
            return new StaffController();
        }
    }

    @RestController
    static class StaffController {
        @GetMapping("/demo/roles/staff")
        String staff() {
            // 실제 관리 작업이나 DB 변경은 하지 않습니다.
            return "staff only";
        }
    }
}