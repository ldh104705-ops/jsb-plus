# 책 대비 개선점 모음

『점프 투 스프링부트』 방식과 비교해 무엇이 나아졌는지 기록합니다. 영상 제작의 재료가 됩니다.

항목 형식:

```markdown
## N. 개선 이름
- 커밋: 000-00
- 교재: 교재는 어떻게 하는지, 무엇이 불편한지
- 개선: 어떻게 바꿨는지 (출처 강의)
- 결과: 확인한 결과 (캡처: docs/captures/파일명.png)
```

## 1. 개발용 DB와 테스트용 DB 분리
- 커밋: 003-01
- 교재: `application.properties` 하나에 DB(`~/local`) 하나. 개발과 테스트가 같은 DB를 써서, 테스트를 돌리면 개발 데이터에 테스트 데이터가 섞인다.
- 개선: 프로필로 설정 파일을 나눈다 (14163 5강)
  - `application-dev.yaml`: `jdbc:h2:./db_dev` (파일, 서버를 꺼도 남음)
  - `application-test.yaml`: `jdbc:h2:mem:db_test` (메모리, 테스트가 끝나면 사라짐)
  - 테스트 클래스에 `@ActiveProfiles("test")`
- 결과: 테스트 로그에 `profile is active: "test"`, `url=jdbc:h2:mem:db_test` / 서버 로그에 `profile is active: "dev"`, `url=jdbc:h2:./db_dev`
  - 캡처: [H2 콘솔에서 개발 DB 접속](captures/003-h2-console-dev-db.jpg)

## 2. BaseEntity로 공통 칸 모으기
- 커밋: 005-01
- 교재: 엔티티마다 id, createDate를 똑같이 반복해서 씀
  ```java
  public class Question { @Id @GeneratedValue(...) private Integer id; private LocalDateTime createDate; ... }
  public class Answer   { @Id @GeneratedValue(...) private Integer id; private LocalDateTime createDate; ... }
  ```
- 개선: `@MappedSuperclass` 부모 클래스에 한 번만 쓰고 물려받음 (14168 2강)
  ```java
  @MappedSuperclass
  public abstract class BaseEntity { @Id @GeneratedValue(...) private int id; private LocalDateTime createDate; ... }
  public class Question extends BaseEntity { ... }
  ```
- 결과: 테이블 구조는 그대로, base_entity 테이블은 생기지 않음. 엔티티가 늘어나도 복사할 필요 없음

## 3. 작성일·수정일 자동 기록 (JPA Auditing)
- 커밋: 005-02
- 교재: 저장할 때마다 사람이 직접 날짜를 넣음 → 깜빡하면 빈칸
  ```java
  q.setCreateDate(LocalDateTime.now());
  ```
- 개선: `@EnableJpaAuditing` + `@CreatedDate` / `@LastModifiedDate` (14168 2강)
- 결과: 날짜를 넣지 않고 저장해도 insert SQL에 create_date, modify_date가 자동으로 들어감 (BaseEntityTest)

## 4. Setter 없이 생성자로 엔티티 만들기
- 커밋: 005-03
- 교재: 엔티티에 `@Setter`, 빈 객체를 만든 뒤 하나씩 채움 (교재도 "편의상 사용"이라고 언급)
  ```java
  Question q = new Question();
  q.setSubject("sbb가 무엇인가요?");
  q.setContent("sbb에 대해서 알고 싶습니다.");
  q.setCreateDate(LocalDateTime.now());
  ```
- 개선: `@Getter`만 두고, 필요한 값은 생성자로 받음 (14168 2강)
  ```java
  Question q = new Question("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
  ```
- 결과: 코드 어디서든 값을 함부로 바꿀 수 없음 (예: `setId(999)` 불가). 값 변경은 이후 `modify()` 같은 이름 있는 메서드로만
