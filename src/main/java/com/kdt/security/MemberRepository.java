// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 저장·조회·삭제 같은 DB 기본 기능을 미리 갖춘 Spring Data JPA 인터페이스입니다.
import org.springframework.data.jpa.repository.JpaRepository;

// 결과가 없을 수도 있음을 안전하게 표현하는 상자입니다. null 대신 사용합니다.
import java.util.Optional;

// Member를 Long 번호로 관리하는 저장소 인터페이스입니다. Spring Data JPA가 구현 객체를 준비합니다.
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 메서드 이름만으로 "login_id가 같은 회원을 찾아라"라는 SQL을 Spring이 자동으로 만듭니다.
    Optional<Member> findByLoginId(String loginId);

    // 같은 방식으로 "api_key가 같은 회원을 찾아라"라는 조회가 만들어집니다. API 키로 누구인지 알아낼 때 씁니다.
    Optional<Member> findByApiKey(String apiKey);

}
