// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 메서드가 만든 객체를 Spring이 관리하도록 등록하는 어노테이션입니다.
import org.springframework.context.annotation.Bean;
// 이 클래스가 설정용 클래스임을 알리는 어노테이션입니다.
import org.springframework.context.annotation.Configuration;
// 비밀번호를 BCrypt 방식으로 암호화하는 도구입니다.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// 비밀번호 암호화 도구들이 따르는 공통 규격(인터페이스)입니다.
import org.springframework.security.crypto.password.PasswordEncoder;

// Spring이 이 클래스를 읽어 설정으로 사용합니다.
@Configuration
public class PasswordConfig {

    // 아래 메서드가 돌려준 객체를 Spring 빈으로 등록해, 필요한 곳에서 가져다 쓰게 합니다.
    @Bean
    // 비밀번호 암호화 도구를 만듭니다. BCrypt는 같은 비밀번호도 매번 다른 해시가 나오고, 되돌릴 수 없습니다.
    public PasswordEncoder passwordEncoder() {
        // BCrypt 암호화 도구를 새로 만들어 돌려줍니다.
        return new BCryptPasswordEncoder();
    }
}
