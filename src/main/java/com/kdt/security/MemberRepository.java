package com.kdt.security;

import org.springframework.data.jpa.repository.JpaRepository;

// Member를 Long 번호로 관리하는 저장소 인터페이스입니다. Spring Data JPA가 구현 객체를 준비합니다.
public interface MemberRepository extends JpaRepository<Member, Long> {
}