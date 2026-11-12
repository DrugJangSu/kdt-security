// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// HttpStatus(404, 400 같은 상태 코드)와 ResponseEntity(상태 코드+본문 묶음)를 쓰기 위해 가져옵니다.
import org.springframework.http.*;
// 입력값 검증(@Valid)에 실패했을 때 Spring이 던지는 예외 클래스입니다.
import org.springframework.web.bind.MethodArgumentNotValidException;
// @RestControllerAdvice, @ExceptionHandler 같은 어노테이션을 쓰기 위해 가져옵니다.
import org.springframework.web.bind.annotation.*;

// MVC 처리 중 지정한 예외를 공통 HTTP 오류 응답으로 바꾸는 클래스입니다.
// 컨트롤러마다 try-catch를 쓰지 않아도, 예외가 나면 이 클래스가 한곳에서 대신 처리해 줍니다.
@RestControllerAdvice
public class ApiExceptionAdvice {
    // MVC 요청 처리 중 NotFoundException이 발생하면 아래 메서드로 404 응답을 만듭니다.
    @ExceptionHandler(NotFoundException.class)
    // 존재하지 않는 회원·글을 조회한 요청에 404를 반환합니다.
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        // 지정한 HTTP 오류 상태와 ApiError 본문을 함께 반환합니다.
        // ex.getMessage()는 예외를 던질 때 적어 둔 문장("post missing" 등)입니다.
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(new ApiError("NOT_FOUND", ex.getMessage()));
    }

    // MVC 요청 처리 중 MethodArgumentNotValidException이 발생하면 아래 메서드로 오류 응답을 만듭니다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    // 요청 DTO 검증 실패를 400 응답으로 바꿉니다.
    public ResponseEntity<ApiError> handleInvalid(MethodArgumentNotValidException ex) {
        // 지정한 HTTP 오류 상태와 ApiError 본문을 함께 반환합니다.
        // 어떤 칸이 틀렸는지는 일부러 알려 주지 않고 "invalid input"만 보냅니다.
        return ResponseEntity.badRequest()
                             .body(new ApiError("INVALID_INPUT", "invalid input"));
    }

    // 로그인 실패 예외(LoginFailedException)가 발생하면 아래 메서드가 처리합니다.
    @ExceptionHandler(LoginFailedException.class)
    // 로그인 실패를 401(인증 실패) 응답으로 바꿉니다.
    public ResponseEntity<ApiError> loginFailed(LoginFailedException ex) {
        // 아이디가 없든 비밀번호가 틀리든 똑같은 문장만 보내서, 공격자가 이유를 추측하지 못하게 합니다.
        return ResponseEntity.status(401).body(new ApiError("LOGIN_FAILED", "login failed"));
    }

    // API 키가 없거나 틀려서 ApiKeyFailedException이 발생하면 아래 메서드가 처리합니다. (인터셉터에서 던진 예외도 여기로 옵니다)
    @ExceptionHandler(ApiKeyFailedException.class)
    // 출입증(API 키)을 확인하지 못한 요청을 401(인증 실패) 응답으로 바꿉니다.
    public ResponseEntity<ApiError> apiKeyFailed(ApiKeyFailedException ex) {
        // 키가 없는지, 틀린 건지는 알려 주지 않고 똑같은 문장만 보냅니다.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError("API_KEY_FAILED", "api key failed"));
    }

    // 권한 없음 예외(ForbiddenException)가 발생하면 아래 메서드가 처리합니다.
    @ExceptionHandler(ForbiddenException.class)
    // 누구인지는 확인됐지만 이 일을 할 권한이 없는 요청(남의 글 수정 등)을 403 응답으로 바꿉니다.
    public ResponseEntity<ApiError> forbidden(ForbiddenException ex) {
        // 403은 "금지됨"입니다. 401(누군지 모름)과 구분해서 보냅니다.
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiError("FORBIDDEN", "forbidden"));
    }

    // JWT 토큰이 없거나 위조·만료·용도 불일치라서 TokenFailedException이 발생하면 아래 메서드가 처리합니다.
    @ExceptionHandler(TokenFailedException.class)
    // 토큰을 믿을 수 없는 요청을 401(인증 실패) 응답으로 바꿉니다.
    public ResponseEntity<ApiError> handleTokenFailed(TokenFailedException ex) {
        // 만료인지 위조인지 알려 주지 않고 똑같은 문장만 보내서, 공격자에게 단서를 주지 않습니다.
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("TOKEN_FAILED", "token failed"));
    }

}
