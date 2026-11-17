package com.kdt.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 요청의 처리 순서와 DB 작업을 담당하는 Service를 Spring 빈으로 등록합니다.
@Service
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class PostService {
    // 글 조회·저장·삭제를 맡기는 저장소입니다.
    private final PostRepository postRepository;
    // MemberRepository로 회원을 조회하거나 저장합니다.
    private final MemberRepository memberRepository;

    // 조회용 트랜잭션을 사용합니다. 연관 객체를 읽어 응답 DTO로 바꾸는 작업도 이 메서드 안에서 마칩니다.
    @Transactional(readOnly = true)
    // 글 목록을 조회해 응답 DTO 목록으로 바꿉니다.
    public List<PostResponse> list() {
        // id 오름차순으로 조회한 각 Post를 toResponse에 전달해 응답용 목록으로 바꿉니다.
        return postRepository.findAll(Sort.by("id").ascending()).stream()
                .map(this::toResponse)
                .toList();
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 요청이 사용할 작성자를 확인하고 새 글을 저장합니다.
    public PostResponse create(String loginId, PostWriteRequest request) {
        // JWT에서 받은 이름이 실제 로컬 회원과 연결되는지 확인합니다.
        Member caller = caller(loginId);
        // 아직 DB에 저장하지 않은 새 글 객체를 메모리에 만듭니다.
        Post post = new Post();
        // 인증된 호출자를 작성자로 지정합니다. 요청 본문에서 받은 회원 번호를 쓰지 않습니다.
        post.setMember(caller);
        // 새 글에 입력 제목을 설정합니다. 아래 setBody는 입력 본문을 설정합니다.
        post.setTitle(request.title());
        post.setBody(request.body());
        // INSERT로 글 번호를 받은 뒤 DTO로 변환합니다. 엔티티 자체를 응답하지 않습니다.
        return toResponse(postRepository.save(post));
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 변경이 허용되는 글을 찾은 뒤 제목과 본문을 바꿉니다.
    public PostResponse update(String loginId, Long id, PostWriteRequest request) {
        // 글 존재와 작성자 일치를 먼저 확인합니다. 통과한 Post에만 변경을 적용합니다.
        Post post = ownPost(loginId, id);
        // 조회한 글의 제목·본문을 바꿉니다. 이 지점까지 필요한 검사를 통과해야 합니다.
        post.setTitle(request.title());
        post.setBody(request.body());
        // DTO를 만들면서 트랜잭션 안에서 연관 회원 정보도 읽습니다. 수정값은 정상 종료 시 DB에 반영됩니다.
        return toResponse(post);
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 삭제 가능한 글을 찾은 뒤 그 한 건만 삭제합니다.
    public void delete(String loginId, Long id) {
        // 작성자 검사를 통과한 글만 삭제합니다. 실패하면 delete를 호출할 대상이 반환되지 않습니다.
        postRepository.delete(ownPost(loginId, id));
    }

    // 검증된 loginId에 해당하는 로컬 회원이 존재하는지 확인합니다.
    private Member caller(String loginId) {
        // 직접 호출 등으로 사용자 식별자가 전달되지 않은 경우도 인증 실패로 처리합니다.
        if (loginId == null || loginId.isBlank()) {
            throw new TokenFailedException();
        }
        // 서명이 맞는 토큰이어도 회원이 DB에서 사라졌으면 글 쓰기는 인증 실패로 거절합니다.
        return memberRepository.findByLoginId(loginId).orElseThrow(TokenFailedException::new);
    }

    // 호출 회원과 글 작성자를 비교해 작업 허용 여부를 결정합니다.
    private Post ownPost(String loginId, Long id) {
        // JWT에서 받은 이름이 실제 로컬 회원과 연결되는지 확인합니다.
        Member caller = caller(loginId);
        // 글 번호로 조회하고 없으면 NotFoundException을 던집니다. 글을 찾은 경우에만 아래 수정·삭제를 실행합니다.
        Post post =
                postRepository.findById(id).orElseThrow(() -> new NotFoundException("post missing"));
        // 작성자 번호와 호출자 번호를 값으로 비교합니다. 다르면 403으로 끝내어 남의 글을 바꾸지 못하게 합니다.
        if (!post.getMember().getId().equals(caller.getId())) {
            throw new ForbiddenException();
        }
        return post;
    }

    // 엔티티 대신 응답에 공개할 값만 꺼냅니다.
    private PostResponse toResponse(Post post) {
        // Post와 작성자에서 공개할 값만 꺼냅니다. 이 DTO에는 비밀번호·API Key가 없습니다.
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getBody(),
                post.getMember().getId(),
                post.getMember().getLoginId());
    }
}