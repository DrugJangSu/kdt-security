package com.kdt.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

// 이 클래스를 DB 데이터와 연결할 JPA 엔티티로 지정합니다.
@Entity
// 실제 DB 테이블 이름과 연결합니다. ddl-auto=none이므로 테이블 변경은 SQL로 따로 합니다.
@Table(name = "sec_member")
// Lombok이 각 필드 값을 읽는 getter를 만듭니다.
@Getter
// Lombok이 필드 값을 바꾸는 setter를 만듭니다. @Setter(AccessLevel.NONE)이 붙은 필드는 제외합니다.
@Setter
// JPA가 조회 결과로 객체를 만들 때 사용할 기본 생성자를 protected로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    // 이 필드는 기본 키이며 새 행 저장 시 DB의 AUTO_INCREMENT 번호를 받습니다.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // DB가 부여하는 id의 setter만 제외해 앱 코드에서 번호를 임의로 바꾸지 않게 합니다.
    @Setter(AccessLevel.NONE)
    // DB의 회원 행을 구분하는 내부 번호입니다. loginId 문자열과 다른 값입니다.
    private Long id;
    // Java loginId를 DB login_id에 연결하고 열 길이·중복·null 조건을 맞춥니다.
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    // 로그인 입력으로 조회할 아이디입니다. DB의 login_id 열에 연결됩니다.
    private String loginId;
    // 대응하는 DB 열의 길이와 null 허용 여부를 지정합니다. 요청 DTO의 입력 검증은 따로 작성합니다.
    @Column(nullable = false, length = 100)
    // 화면에 보여 줄 이름입니다. 본인 확인의 기준값으로 사용하지 않습니다.
    private String nickname;

    @Column(length = 100)
    private String password;

    @Column(name = "api_key", length = 36, unique = true)
    private String apiKey;
}