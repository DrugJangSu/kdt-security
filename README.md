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
