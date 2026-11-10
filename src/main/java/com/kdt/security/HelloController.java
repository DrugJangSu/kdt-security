package com.kdt.security;

import org.springframework.web.bind.annotation.*;

// 이 클래스의 메서드가 반환한 값을 HTTP 응답 본문으로 보냅니다.
@RestController
public class HelloController {
    // GET /hello 요청을 바로 아래 메서드에 연결합니다.
    @GetMapping("/hello")
    public String hello() { return "kimbap"; }
}