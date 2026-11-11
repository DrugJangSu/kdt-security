// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 저장·조회·삭제 같은 DB 기본 기능을 미리 갖춘 Spring Data JPA 인터페이스입니다.
import org.springframework.data.jpa.repository.JpaRepository;

// Post를 Long 번호로 관리합니다. findAll·findById·save·delete 같은 기본 기능을 상속받습니다.
// 안이 비어 있어도 상속만으로 기능이 생기며, 구현은 Spring이 알아서 만듭니다.
public interface PostRepository extends JpaRepository<Post, Long> {
}
