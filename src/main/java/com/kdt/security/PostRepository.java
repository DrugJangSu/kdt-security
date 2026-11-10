package com.kdt.security;

import org.springframework.data.jpa.repository.JpaRepository;

// Post를 Long 번호로 관리합니다. findAll·findById·save·delete 같은 기본 기능을 상속받습니다.
public interface PostRepository extends JpaRepository<Post, Long> {
}