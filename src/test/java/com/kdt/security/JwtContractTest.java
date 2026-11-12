package com.kdt.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// 앱 전체를 H2(test 프로필)로 띄우고 가짜 손님(MockMvc)으로 JWT API를 검사합니다.
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class JwtContractTest {

    @Autowired MockMvc mvc;
    // 진짜 앱이 쓰는 토큰 기계입니다. 테스트용 토큰을 직접 만들 때 씁니다.
    @Autowired JwtService jwt;
    // 진짜 앱과 같은 비밀 키입니다. 만료 토큰을 "같은 도장"으로 만들 때 씁니다.
    @Value("${kdt.jwt.secret}") String secret;

    // 재발급 요청 JSON을 만드는 도우미입니다.
    private String refreshBody(String token) {
        return "{\"refreshToken\":\"" + token + "\"}";
    }

    // ① 정상 access로 /me → 200, kdt01
    @Test
    void accessOpensMe() throws Exception {
        String access = jwt.createAccess("kdt01");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + access))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("kdt01"));
    }

    // ② refresh로 /me → 401 (용도가 다름)
    @Test
    void refreshCannotOpenMe() throws Exception {
        String refresh = jwt.createRefresh("kdt01");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + refresh))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ③ "Bearer " 뒤가 비었음 → 500이 아니라 401
    @Test
    void emptyBearerRejected() throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ④ refresh로 재발급 → 200, 새 토큰 2개
    @Test
    void refreshCreatesPair() throws Exception {
        String refresh = jwt.createRefresh("kdt01");
        mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(refreshBody(refresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    // ⑤ 이미 만료된 access → 401 (같은 키, 수명 -60초)
    @Test
    void expiredAccessIsRejected() throws Exception {
        String expired = new JwtService(secret, -60, 604800).createAccess("kdt01");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ⑥ 다른 키로 찍은 access → 401 (도장이 다름)
    @Test
    void anotherSigningKeyIsRejected() throws Exception {
        String otherKey = "z".repeat(48); // 테스트 전용 가짜 키 (32바이트 이상)
        String forged = new JwtService(otherKey, 900, 604800).createAccess("kdt01");
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ⑦ access로 재발급 → 401 (용도가 다름)
    @Test
    void accessCannotRefresh() throws Exception {
        String access = jwt.createAccess("kdt01");
        mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(refreshBody(access)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ⑧ 이미 만료된 refresh → 401
    @Test
    void expiredRefreshIsRejected() throws Exception {
        String expired = new JwtService(secret, 900, -60).createRefresh("kdt01");
        mvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(refreshBody(expired)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_FAILED"));
    }

    // ⑨ 같은 refresh로 두 번 재발급 → 둘 다 200 (지금 구현의 한계)
    @Test
    void previousRefreshStillWorksAfterReissue() throws Exception {
        String refresh = jwt.createRefresh("kdt01");
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/v1/auth/token/refresh")
                            .contentType("application/json")
                            .content(refreshBody(refresh)))
                    .andExpect(status().isOk());
        }
    }
}