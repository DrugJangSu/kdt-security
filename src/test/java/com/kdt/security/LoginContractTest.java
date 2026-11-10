package com.kdt.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class LoginContractTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Test
    void success() throws Exception {
        String credential = System.getenv("TEST_LOGIN_PASSWORD");
        org.junit.jupiter.api.Assertions.assertNotNull(credential,
                "Set TEST_LOGIN_PASSWORD in the local test run configuration.");
        org.junit.jupiter.api.Assertions.assertFalse(credential.isBlank(),
                "TEST_LOGIN_PASSWORD must not be blank.");
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(java.util.Map.of(
                                        "loginId", "kdt01",
                                        "password", credential))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("kdt01"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
