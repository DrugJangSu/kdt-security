package com.kdt.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

// 앱 설정·빈·Runner를 올리되 test 프로필의 H2를 사용하는 통합 테스트입니다.
@SpringBootTest
// application-test.yml의 H2 설정으로 실행해 본문 MySQL 데이터를 사용하지 않습니다.
@ActiveProfiles("test")
// 실제 서버에 접속하지 않고 MVC 요청·응답을 확인할 MockMvc를 준비합니다.
@AutoConfigureMockMvc
class LoginContractTest {
    // 테스트 요청을 실행할 MockMvc를 주입받습니다. 실제 서버로 네트워크 요청을 보내지 않습니다.
    @Autowired MockMvc mvc;

    // @Test가 붙은 메서드 하나가 테스트 한 개입니다.
    @Test
    // 정상 입력의 상태·회원 정보와 비밀번호 비노출을 확인합니다.
    void success() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("{\"loginId\":\"kdt01\",\"password\":\"kimbap12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("kdt01"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
        // 틀린 비밀번호가 같은 로그인 실패 응답으로 끝나는지 확인합니다.
    void wrongPassword() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("{\"loginId\":\"kdt01\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
        // 빈 JSON 입력이 비밀번호 비교 전에 거절되는지 확인합니다.
    void blankInput() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        mvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content("{\"loginId\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }
}