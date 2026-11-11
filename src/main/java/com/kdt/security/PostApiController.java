// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// final 필드를 받는 생성자를 Lombok이 자동으로 만들어 주는 어노테이션입니다.
import lombok.RequiredArgsConstructor;

// 글 여러 개를 목록으로 담기 위해 가져옵니다.
import java.util.List;
// 요청으로 받은 값이 규칙을 지키는지 검사하게 해 주는 어노테이션입니다.
import jakarta.validation.Valid;
// Swagger 문서에서 각 API의 설명을 적을 때 쓰는 어노테이션입니다.
import io.swagger.v3.oas.annotations.Operation;
// Swagger 문서에서 API를 이름별로 묶어 보여 줄 때 쓰는 어노테이션입니다.
import io.swagger.v3.oas.annotations.tags.Tag;
// 201, 204 같은 HTTP 상태 코드를 이름으로 쓰기 위해 가져옵니다.
import org.springframework.http.HttpStatus;
// @GetMapping, @PostMapping, @PathVariable 등 웹 요청용 어노테이션을 한꺼번에 가져옵니다.
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

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "List posts")
    // GET /api/v1/posts 요청을 바로 아래 메서드에 연결합니다.
    @GetMapping
    // GET 목록 요청을 서비스에 연결합니다.
    // 목록 조회는 서비스가 수행하고, 반환된 DTO 목록이 JSON 응답이 됩니다.
    public List<PostResponse> list() { return postService.list(); }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Create post")
    // POST /api/v1/posts 요청을 바로 아래 메서드에 연결합니다.
    @PostMapping
    // 새 글 저장 성공은 201 Created로 응답합니다.
    @ResponseStatus(HttpStatus.CREATED)
    // 검증된 입력으로 글을 만들고 생성 결과를 응답합니다.
    // Spring이 요청 JSON을 DTO로 바꾸고, @Valid로 입력을 검사한 뒤 이 메서드를 호출합니다.
    public PostResponse create(@Valid @RequestBody PostCreateRequest request) {
        // 현재는 입력 DTO의 작성자 번호와 글 내용을 전달합니다. 05에서는 로그인한 회원을 작성자로 정합니다.
        return postService.create(request);
    }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Update post")
    // PUT /api/v1/posts/{id} 요청을 바로 아래 메서드에 연결합니다.
    @PutMapping("/{id}")
    // URL의 글 번호와 수정 입력을 서비스에 전달합니다.
    // id는 URL의 {id} 값입니다. JSON 안의 회원 번호나 필드와 별도로 받습니다.
    public PostResponse update(@PathVariable Long id,
                               // JSON을 요청 DTO로 바꾸고 @NotBlank·@Size 등의 입력 검증을 수행합니다.
                               @Valid @RequestBody PostUpdateRequest request) {
        // 서비스가 글 조회·변경을 마친 결과를 돌려줍니다. 작성자 검사는 02에서 추가합니다.
        return postService.update(id, request);
    }

    // Swagger 문서에 표시될 설명입니다.
    @Operation(summary = "Delete post")
    // DELETE /api/v1/posts/{id} 요청을 바로 아래 메서드에 연결합니다.
    @DeleteMapping("/{id}")
    // 삭제 성공은 204 No Content로 응답합니다. 성공 본문은 없습니다.
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // 삭제가 성공하면 상태 코드만 보내고 본문은 비웁니다.
    // 서비스가 정상 종료해야 204로 끝납니다. 예외가 나면 해당 실패 응답으로 바뀝니다.
    // id는 URL의 {id} 값입니다. JSON 안의 회원 번호나 필드와 별도로 받습니다.
    public void delete(@PathVariable Long id) { postService.delete(id); }
}
