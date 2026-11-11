// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;

// 글 여러 개를 목록으로 담기 위해 가져옵니다.
import java.util.List;
// 조회 결과의 정렬 기준(id 순서 등)을 지정하는 도구입니다.
import org.springframework.data.domain.Sort;
// 이 클래스를 "서비스"로 Spring에 등록하는 어노테이션입니다.
import org.springframework.stereotype.Service;
// DB 작업을 한 묶음(트랜잭션)으로 처리하게 하는 어노테이션입니다.
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
                             .map(this::toResponse).toList();
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 요청이 사용할 작성자를 확인하고 새 글을 저장합니다.
    public PostResponse create(PostCreateRequest request) {
        // 현재 단계는 request.memberId로 작성자를 찾습니다. 없는 번호면 저장하지 않고 404가 됩니다.
        Member member = memberRepository.findById(request.memberId())
                                        .orElseThrow(() -> new NotFoundException("member missing"));
        // 아직 DB에 저장하지 않은 새 글 객체를 메모리에 만듭니다.
        Post post = new Post();
        // 앞에서 조회한 회원을 글 작성자로 연결합니다. DB에는 이 회원의 id가 저장됩니다.
        post.setMember(member);
        // 새 글에 입력 제목을 설정합니다. 아래 setBody는 입력 본문을 설정합니다.
        post.setTitle(request.title());
        // 새 글에 입력 본문을 설정합니다.
        post.setBody(request.body());
        // INSERT로 글 번호를 받은 뒤 DTO로 변환합니다. 엔티티 자체를 응답하지 않습니다.
        return toResponse(postRepository.save(post));
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 이 준비 단계는 번호로 수정합니다. 작성자 검사는 02에서 추가합니다.
    // 메서드 실행 중 필드를 바꾼 순간과 트랜잭션이 정상 종료돼 DB에 반영된 순간은 구분합니다.
    public PostResponse update(Long id, PostUpdateRequest request) {
        // 글 번호로 조회하고 없으면 NotFoundException을 던집니다. 글을 찾은 경우에만 아래 수정·삭제를 실행합니다.
        Post post = postRepository.findById(id)
                                  .orElseThrow(() -> new NotFoundException("post missing"));
        // 조회한 글의 제목·본문을 바꿉니다. 이 지점까지 필요한 검사를 통과해야 합니다.
        post.setTitle(request.title());
        // 본문도 새 값으로 바꿉니다. save를 따로 부르지 않아도 트랜잭션이 끝날 때 자동으로 DB에 반영됩니다.
        post.setBody(request.body());
        // DTO를 만들면서 트랜잭션 안에서 연관 회원 정보도 읽습니다. 수정값은 정상 종료 시 DB에 반영됩니다.
        return toResponse(post);
    }

    // 이 메서드의 DB 작업을 한 트랜잭션으로 처리합니다. 정상 종료되면 커밋하고, RuntimeException이 밖으로 전달되면 롤백합니다.
    @Transactional
    // 이 준비 단계는 번호로 삭제합니다. 호출자 인증 검사는 아직 없습니다.
    public void delete(Long id) {
        // 글 번호로 조회하고 없으면 NotFoundException을 던집니다. 글을 찾은 경우에만 아래 수정·삭제를 실행합니다.
        Post post = postRepository.findById(id)
                                  .orElseThrow(() -> new NotFoundException("post missing"));
        // 찾아 둔 글 한 건을 삭제합니다. 삭제 후 같은 id의 글은 목록에 없어야 합니다.
        postRepository.delete(post);
    }

    // 엔티티 대신 응답에 공개할 값만 꺼냅니다.
    private PostResponse toResponse(Post post) {
        // Post와 작성자에서 공개할 값만 꺼냅니다. 이 DTO에는 비밀번호·API Key가 없습니다.
        return new PostResponse(post.getId(), post.getTitle(), post.getBody(),
                                post.getMember().getId(), post.getMember().getLoginId());
    }
}
