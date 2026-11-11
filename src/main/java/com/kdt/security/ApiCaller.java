// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 각 필드의 값을 읽는 getXxx() 메서드를 자동으로 만들어 주는 어노테이션입니다.
import lombok.Getter;
// 각 필드의 값을 바꾸는 setXxx() 메서드를 자동으로 만들어 주는 어노테이션입니다.
import lombok.Setter;
// Spring이 이 클래스를 찾아 관리하게 하는 어노테이션입니다.
import org.springframework.stereotype.Component;
// 요청 한 번마다 새 객체를 만들어 쓰게 하는 어노테이션을 가져옵니다.
import org.springframework.web.context.annotation.RequestScope;

// "지금 요청을 보낸 사람이 누구인지" 적어 두는 쪽지 역할의 클래스입니다.
// Spring이 관리하는 객체(빈)로 등록합니다.
@Component
// Lombok이 getMember()를 만들어 줍니다. 다른 곳에서 쪽지를 읽을 때 씁니다.
@Getter
// Lombok이 setMember()를 만들어 줍니다. 인터셉터가 쪽지를 쓸 때 씁니다.
@Setter
// 요청이 올 때마다 새 쪽지를 만들고 요청이 끝나면 버립니다. 그래서 다른 사람의 요청과 섞이지 않습니다.
@RequestScope
public class ApiCaller {

    // 이번 요청을 보낸 회원입니다. API 키 검사를 통과하기 전에는 비어(null) 있습니다.
    private Member member;
}
