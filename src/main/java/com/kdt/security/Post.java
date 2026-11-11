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
@Table(name = "sec_post")
// Lombok이 각 필드 값을 읽는 getter를 만듭니다.
@Getter
// Lombok이 필드 값을 바꾸는 setter를 만듭니다. @Setter(AccessLevel.NONE)이 붙은 필드는 제외합니다.
@Setter
// JPA가 조회 결과로 객체를 만들 때 사용할 기본 생성자를 protected로 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {
  // 이 필드는 기본 키이며 새 행 저장 시 DB의 AUTO_INCREMENT 번호를 받습니다.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // DB가 부여하는 id의 setter만 제외해 앱 코드에서 번호를 임의로 바꾸지 않게 합니다.
  @Setter(AccessLevel.NONE)
  // 글 한 건의 DB 번호입니다. 수정·삭제 URL의 id와 연결됩니다.
  private Long id;

  // 대응하는 DB 열의 길이와 null 허용 여부를 지정합니다. 요청 DTO의 입력 검증은 따로 작성합니다.
  @Column(nullable = false, length = 200)
  // 요청에서 받은 글 제목을 저장합니다.
  private String title;

  // 대응하는 DB 열의 길이와 null 허용 여부를 지정합니다. 요청 DTO의 입력 검증은 따로 작성합니다.
  // TEXT는 긴 글을 담을 수 있는 DB 자료형입니다.
  @Column(nullable = false, columnDefinition = "TEXT")
  // 글의 본문 문자열입니다. HTTP 요청 전체를 뜻하는 body와 구분합니다.
  private String body;

  // 여러 글이 한 회원을 작성자로 참조합니다. 회원 정보는 필요할 때 조회할 수 있습니다.
  // LAZY는 "필요할 때까지 회원 정보를 읽지 않는다", optional=false는 "작성자 없는 글은 없다"는 뜻입니다.
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  // member 객체의 관계를 DB의 member_id 외래 키 열로 연결합니다.
  @JoinColumn(name = "member_id", nullable = false)
  // 이 글의 작성자 객체입니다. DB에는 member_id 외래 키로 저장됩니다.
  private Member member;
}
