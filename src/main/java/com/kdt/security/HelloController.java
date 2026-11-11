// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// @RestController, @GetMapping 등 웹 요청용 어노테이션을 한꺼번에 가져옵니다.
import org.springframework.web.bind.annotation.*;

// 이 클래스의 메서드가 반환한 값을 HTTP 응답 본문으로 보냅니다.
@RestController
public class HelloController {
    // GET /hello 요청을 바로 아래 메서드에 연결합니다.
    @GetMapping("/hello")
    // 서버가 잘 켜졌는지 확인하는 용도로 "kimbap" 글자를 그대로 돌려줍니다.
    public String hello() { return "kimbap"; }
}
