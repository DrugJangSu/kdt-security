package com.kdt.security;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Swagger 문서에 표시할 이름과 설명입니다. 요청의 인증·인가 규칙은 별도로 작성합니다.
@Tag(name = "posts")
// 이 클래스의 메서드가 반환한 값을 HTTP 응답 본문으로 보냅니다.
@RestController
// 각 메서드의 경로 앞에 붙일 공통 URL 경로입니다.
@RequestMapping("/api/v1/posts")
// Lombok이 초기값 없는 final 필드를 인자로 받는 생성자를 만듭니다.
// Spring이 이 생성자에 필요한 빈을 전달합니다. 이미 초기값이 있는 필드는 생성자 인자에서 제외합니다.
@RequiredArgsConstructor
public class PostApiController {
    // Controller가 받은 글 요청을 실제 조회·변경 처리로 연결합니다.
    private final PostService postService;

    @Operation(summary = "List posts")
    // GET /api/v1/posts 요청을 바로 아래 메서드에 연결합니다.
    @GetMapping
    // GET 목록 요청을 서비스에 연결합니다.
    public List<PostResponse> list() {
        // 목록 조회는 서비스가 수행하고, 반환된 DTO 목록이 JSON 응답이 됩니다.
        return postService.list();
    }

    @Operation(summary = "Create own post")
    // POST /api/v1/posts 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping
    // 새 글 저장 성공은 201 Created로 응답합니다.
    @ResponseStatus(HttpStatus.CREATED)
    // 검증된 입력으로 글을 만들고 생성 결과를 응답합니다.
    public PostResponse create(
            // JWT 필터가 검증해 Authentication에 넣은 loginId를 받습니다. 입력 JSON의 아이디가 아닙니다.
            // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
            @AuthenticationPrincipal String loginId, @Valid @RequestBody PostWriteRequest request) {
        // 인증된 이름과 글 입력을 따로 전달합니다. 클라이언트가 작성자를 선택하지 못하게 합니다.
        return postService.create(loginId, request);
    }

    @Operation(summary = "Update own post")
    // PUT /api/v1/posts/{id} 요청을 바로 아래 메서드에 연결합니다.
    @PutMapping("/{id}")
    // URL의 글 번호와 수정 입력을 서비스에 전달합니다.
    public PostResponse update(
            // JWT 필터가 검증해 Authentication에 넣은 loginId를 받습니다. 입력 JSON의 아이디가 아닙니다.
            @AuthenticationPrincipal String loginId,
            // URL의 {id}를 글 번호 인자로 받습니다. JSON의 값과는 별개입니다.
            @PathVariable Long id,
            // JSON을 요청 DTO로 바꾸고 @NotBlank·@Size 등의 입력 검증을 수행합니다.
            @Valid @RequestBody PostWriteRequest request) {
        // 서비스가 조회·작성자 검사·변경을 마친 결과를 돌려줍니다. 예외가 나면 성공 DTO를 보내지 않습니다.
        return postService.update(loginId, id, request);
    }

    @Operation(summary = "Delete own post")
    // DELETE /api/v1/posts/{id} 요청을 바로 아래 메서드에 연결합니다.
    @DeleteMapping("/{id}")
    // 삭제 성공은 204 No Content로 응답합니다. 성공 본문은 없습니다.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // 삭제가 성공하면 상태 코드만 보내고 본문은 비웁니다.
    // id는 URL의 {id} 값입니다. JSON 안의 회원 번호나 필드와 별도로 받습니다.
    // loginId는 JWT 필터가 인증한 principal입니다. 요청 JSON의 아이디를 신뢰해 넣지 않습니다.
    public void delete(@AuthenticationPrincipal String loginId, @PathVariable Long id) {
        // 서비스가 정상 종료해야 204로 끝납니다. 예외가 나면 해당 실패 응답으로 바뀝니다.
        postService.delete(loginId, id);
    }
}