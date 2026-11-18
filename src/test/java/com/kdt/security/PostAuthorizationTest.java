package com.kdt.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;

// 앱 설정·빈·Runner를 올리되 test 프로필의 H2를 사용하는 통합 테스트입니다.
@SpringBootTest
// application-test.yml의 H2 설정으로 실행해 본문 MySQL 데이터를 사용하지 않습니다.
@ActiveProfiles("test")
// 실제 서버에 접속하지 않고 MVC 요청·응답을 확인할 MockMvc를 준비합니다.
@AutoConfigureMockMvc
class PostAuthorizationTest {
    // 테스트 요청을 실행할 MockMvc를 주입받습니다. 실제 서버로 네트워크 요청을 보내지 않습니다.
    @Autowired MockMvc mvc;
    // 테스트용 토큰을 발급할 JwtService 빈을 주입받습니다.
    @Autowired JwtService jwt;
    // 요청 전후의 글 저장값을 확인할 Repository를 주입받습니다.
    @Autowired PostRepository posts;
    // 응답 JSON에서 실제로 생성된 글 id를 읽는 도구입니다.
    private final JsonMapper json = JsonMapper.builder().build();

    // @Test가 붙은 메서드 하나가 테스트 한 개입니다.
    @Test
    // 인증 없는 글 쓰기가 입력 처리 전에 거절되는지 확인합니다.
    void bearerIsRequired() throws Exception {
        // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
        mvc.perform(
                        post("/api/v1/posts")
                                .contentType("application/json")
                                .content("{\"title\":\"test\",\"body\":\"body\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
        // 403 응답 뒤 실제 저장값도 변경되지 않았는지 확인합니다.
    void otherMemberCannotChangeData() throws Exception {
        // 테스트에서 사용할 액세스 토큰을 직접 발급합니다. 아이디·비밀번호로 /token을 호출하는 과정은 포함하지 않습니다.
        String owner = "Bearer " + jwt.createAccess("kdt01");
        // 서명한 소유자 토큰으로 새 연습 글을 만든 뒤 그 응답을 보관합니다.
        var response =
                // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
                mvc.perform(
                                post("/api/v1/posts")
                                        .header("Authorization", owner)
                                        .contentType("application/json")
                                        .content("{\"title\":\"original\",\"body\":\"body\"}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        // 응답의 실제 id를 사용합니다. 기존 시드의 1번 글을 수정하는 테스트가 아닙니다.
        long id = json.readTree(response).get("id").asLong();
        try {
            // MockMvc로 요청을 실행하고 andExpect로 응답 상태와 본문을 검사합니다.
            mvc.perform(
                            put("/api/v1/posts/" + id)
                                    .header("Authorization", "Bearer " + jwt.createAccess("kdt02"))
                                    .contentType("application/json")
                                    .content("{\"title\":\"blocked\",\"body\":\"blocked\"}"))
                    // 거절 상태를 확인합니다. 아래 DB 조회까지 있어야 실제 데이터 미변경도 확인할 수 있습니다.
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"));
            // 403이 나왔어도 DB 값이 바뀌었다면 이 검증에서 실패합니다.
            assertEquals("original", posts.findById(id).orElseThrow().getTitle());
        } finally {
            // 검증 성공·실패와 관계없이 이 테스트가 만든 글만 정리합니다.
            // 방금 만든 연습 글만 삭제합니다. 다른 시드 데이터는 삭제하지 않습니다.
            posts.deleteById(id);
        }
    }
    @Test
        // 별도 headers 설정을 추가하지 않아도 기본 보안 헤더가 응답에 포함되는지 확인합니다.
    void defaultSecurityHeadersArePresent() throws Exception {
        mvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Cache-Control", containsString("no-store")));
    }
}