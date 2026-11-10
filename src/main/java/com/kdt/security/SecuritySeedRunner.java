package com.kdt.security;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// 여러 시작 Runner 중 회원·글 준비를 먼저 실행합니다. 빈의 생성 순서를 지정하는 표시는 아닙니다.
@Order(1)
// Spring이 이 클래스를 찾아 관리할 빈으로 등록합니다.
@Component
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class SecuritySeedRunner implements ApplicationRunner {
    // MemberRepository로 회원을 조회하거나 저장합니다.
    private final MemberRepository memberRepository;
    // 글 조회·저장·삭제를 맡기는 저장소입니다.
    private final PostRepository postRepository;

    @Override
    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 회원 준비 뒤, 글이 하나도 없을 때만 연습 글 세 건을 넣습니다.
    public void run(ApplicationArguments args) {
        // kdt01이 이미 있으면 재사용하고 없을 때만 만듭니다. 아래 other도 같은 규칙입니다.
        Member first = member("kdt01", "kimbap");
        Member other = member("kdt02", "nori");
        // 글이 하나도 없는 첫 준비 상태에서만 세 건을 추가합니다. 일부 삭제한 글을 매번 복원하는 조건은 아닙니다.
        if (postRepository.count() == 0) {
            post(first, "첫 번째 글", "kdt01의 글입니다.");
            post(first, "수정 연습", "수정과 삭제에 사용할 글입니다.");
            post(other, "다른 회원의 글", "작성자 권한을 확인할 때 사용합니다.");
        }
    }

    // 같은 loginId의 회원을 재사용하고 없을 때만 새로 저장합니다.
    private Member member(String loginId, String nickname) {
        // 아직 전용 조회 메서드가 없는 준비 코드이므로 기본 findAll에서 같은 loginId를 찾습니다.
        return memberRepository.findAll().stream()
                               // 같은 아이디인 회원만 고릅니다. 바로 뒤 findFirst로 있으면 한 건을 가져옵니다.
                               .filter(m -> m.getLoginId().equals(loginId)).findFirst()
                               // 일치한 회원이 없을 때만 아래 람다를 실행해 새 회원을 저장합니다.
                               .orElseGet(() -> {
                                   Member member = new Member();
                                   member.setLoginId(loginId);
                                   member.setNickname(nickname);
                                   // 새 회원을 저장하고 DB가 생성한 번호를 가진 객체를 반환합니다.
                                   return memberRepository.save(member);
                               });
    }

    // 제목·본문·작성자가 정해진 연습 글 한 건을 저장합니다.
    private void post(Member member, String title, String body) {
        Post post = new Post();
        post.setMember(member);
        post.setTitle(title);
        post.setBody(body);
        // 작성자·제목·본문을 채운 연습 글을 DB에 저장합니다.
        postRepository.save(post);
    }
}