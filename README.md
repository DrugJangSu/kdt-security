# kdt-security

데브코스 부트캠프 Spring Security 실습 프로젝트입니다.

실제 `application*.yaml/yml/properties`, `.env`, 인증키, 로그, 빌드·IDE 파일은 Git에서 제외합니다. `application-example.yaml`은 공유 가능한 설정 예제입니다. 비밀번호와 API 키는 환경변수로 관리하세요.

현재 PC에서는 아래 명령으로 커밋·푸시할 때 Secret check가 자동 실행됩니다.

```bash
git add .
git commit -m "update"
git push
```

검사에 걸리면 값을 제외된 로컬 설정이나 환경변수로 옮기세요. 자동 검사는 모든 비밀정보와 개인정보의 탐지를 보장하지 않습니다.

새로 복제한 PC에서는 Python 3와 Gitleaks를 설치한 뒤 `sh scripts/install-hooks.sh`를 실행해야 자동 검사가 활성화됩니다.

## HTTP 로그인 테스트

IntelliJ에서 `src/test/http/01-login.http`를 열고 실행 환경을 `local`로 선택하세요. 실제 테스트 값은 같은 폴더의 `http-client.private.env.json`에 저장하며 Git에서 제외됩니다. `http-client.env.json`은 공유용 예시 값입니다. 새로 복제한 환경에서는 private 파일에 `local` 환경의 `testLoginPassword`, `testWrongPassword`를 설정하세요.

### Java 로그인 테스트

`LoginContractTest` 실행 시 IntelliJ의 해당 테스트 실행 설정 → 환경변수에 `TEST_LOGIN_PASSWORD`를 설정하세요. 값은 실습 DB에 등록된 로그인 계정의 테스트 비밀번호이며, DB 접속 비밀번호와는 별개입니다. HTTP Client의 `local` 환경 파일은 Java 테스트에 자동 전달되지 않습니다.
