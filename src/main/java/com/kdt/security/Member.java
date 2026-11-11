// 이 파일이 들어 있는 폴더(패키지) 이름입니다.
package com.kdt.security;

// 접근 범위(public, protected 등)를 Lombok에게 지정할 때 쓰는 값입니다.
import lombok.AccessLevel;
// 각 필드의 값을 읽는 getXxx() 메서드를 자동으로 만들어 주는 어노테이션입니다.
import lombok.Getter;
// 매개변수 없는 기본 생성자를 자동으로 만들어 주는 어노테이션입니다.
import lombok.NoArgsConstructor;
// 각 필드의 값을 바꾸는 setXxx() 메서드를 자동으로 만들어 주는 어노테이션입니다.
import lombok.Setter;

// @Entity, @Id, @Column 등 DB와 연결하는 JPA 어노테이션을 한꺼번에 가져옵니다.
import jakarta.persistence.*;

// 이 클래스를 DB 데이터와 연결할 JPA 엔티티로 지정합니다. 객체 하나가 표의 한 줄(행)입니다.
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
    // (비어 있으면 안 되고, 같은 아이디가 두 번 들어갈 수 없고, 50자까지)
    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    // 로그인 입력으로 조회할 아이디입니다. DB의 login_id 열에 연결됩니다.
    private String loginId;
    // 대응하는 DB 열의 길이와 null 허용 여부를 지정합니다. 요청 DTO의 입력 검증은 따로 작성합니다.
    @Column(nullable = false, length = 100)
    // 화면에 보여 줄 이름입니다. 본인 확인의 기준값으로 사용하지 않습니다.
    private String nickname;

    // 비밀번호 칸입니다. 평문이 아니라 BCrypt 해시가 들어가며, 비어 있을 수 있고 100자까지입니다.
    @Column(length = 100)
    // 저장된 비밀번호 해시 문자열입니다. 로그인할 때 입력값과 비교하는 기준입니다.
    private String password;

    // API 키 칸입니다. UUID는 36자이고, 같은 키가 두 명에게 발급되지 않도록 중복을 막습니다.
    @Column(name = "api_key", length = 36, unique = true)
    // 로그인 대신 사용할 수 있는 임시 열쇠 문자열입니다. 아직 발급받지 않았다면 null입니다.
    private String apiKey;
}
