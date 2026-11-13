# 강의 14163 "점프 투 스프링부트 개선" 분석 리포트

- 강의 페이지: https://www.slog.gg/p/14163/raw (880줄 전체 확인)
- 저장소: https://github.com/jhs512/p-14163-2 (clone 후 전체 커밋/파일 확인, 커밋 35개, 파일 26개)
- 구버전 저장소: https://github.com/jhs512/p-13959-1 (강의 페이지에 "커밋" 링크로 병기됨. 이번 분석 대상 아님)

## 0. 먼저 알아둘 점 (중요)
- **진행 범위가 아직 앞부분이다.** 마지막 커밋 `039` = 책 2-14(부트스트랩으로 화면 꾸미기)까지. 강의 페이지 끝의 숨김 블록(`$$hide`)에 2-15 ~ 3-13 목차가 예고만 되어 있음(이후 미구현).
- 그래서 요청하신 개선 항목 중 **다음 내용은 저장소/강의에 존재하지 않는다**: BaseEntity/@MappedSuperclass, JPA Auditing, Rq/RsData, `@Transactional(readOnly)`, DTO/form record, Validation, 전역 예외 처리(@ControllerAdvice), Spring Security, application-secret.yml, prod 프로파일, MockMvc 컨트롤러 테스트, Thymeleaf layout, README. (향후 강의에서 나올 수 있으나 현재 미확인)
- 패키지 구조도 `domain/question/question/entity` 같은 깊은 구조가 **아니고**, 책과 같은 `com.mysite.sbb.question`, `com.mysite.sbb.answer` 수준의 도메인별 패키지다.
- 커밋 메시지는 **설명 없이 강의 번호만** 적혀 있다(`002`, `018-1` 등). 각 커밋에 같은 이름의 git tag도 붙어 있다(강의 페이지에서 `commit/018-1` 형태로 링크).

## 1. 기술 스택 / 버전
출처: `build.gradle.kts`, `settings.gradle.kts`, `gradle/wrapper/gradle-wrapper.properties`, `src/main/resources/application*.yaml`

| 항목 | 값 |
|---|---|
| Java | toolchain **25** |
| Spring Boot | **4.1.2** (`io.spring.dependency-management` 1.1.7) |
| 빌드 | **Gradle Kotlin DSL** (`build.gradle.kts`), Gradle wrapper **9.4.1** |
| rootProject.name | `sbb`, group `com.mysite`, 메인 클래스 `com.mysite.sbb.SbbApplication` |
| 웹 | `spring-boot-starter-webmvc` (Boot 4 의 새 스타터명) |
| JPA | `spring-boot-starter-data-jpa` |
| 템플릿 | `spring-boot-starter-thymeleaf` |
| DB | H2 (`runtimeOnly com.h2database:h2`) + `spring-boot-h2console` (Boot 4에서 h2 콘솔이 별도 모듈) |
| Lombok | compileOnly + annotationProcessor (test 쪽도 동일) |
| devtools | `developmentOnly` |
| 테스트 | `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`, `spring-boot-starter-thymeleaf-test`, junit-platform-launcher |
| 설정 파일 형식 | **`.yaml`** (책은 `application.properties`) |
| 프론트 | Bootstrap 5.3.8 (cdnjs CDN), Pretendard 폰트(jsdelivr CDN) |

build.gradle.kts 의존성은 주석으로 그룹핑(`// Spring Boot`, `// Database`, `// Lombok`, `// Development`, `// Test`) — 028 커밋에서 정리.

### application.yaml (공통)
```yaml
spring:
  output:
    ansi:
      enabled: always
  application:
    name: sbb
  profiles:
    active: dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
        highlight_sql: true
        use_sql_comments: true
logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE
    org.hibernate.orm.jdbc.extract: TRACE
    org.springframework.transaction.interceptor: TRACE
```
### application-dev.yaml
```yaml
spring:
  thymeleaf:
    cache: false
  datasource:
    url: jdbc:h2:./db_dev
    username: sa
    password:
    driver-class-name: org.h2.Driver
```
### application-test.yaml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:db_test
    username: sa
    password:
    driver-class-name: org.h2.Driver
```
`.gitignore` 에 `### Custom ###` 섹션으로 `db_dev.mv.db`, `db_dev.trace.db`, `db_dev.lock.db` 추가.

## 2. 강의 구성 (강의 번호 = 커밋/태그 번호)
| 강 | 내용 | 책 챕터 | 커밋 |
|---|---|---|---|
| 1 | JSB 소개 | - | - |
| 2 | 프로젝트 세팅(이니셜라이저/인텔리제이), 메인 클래스 이름 변경, GIT PUSH | 1장 | 002 |
| 3 | MainController 추가, 액션메서드 2개(`/`, `/hello`) — void라 500 나는 과정, DispatcherServlet/HandlerMapping 흐름(UML), HTTP 응답코드 | 2-02 | 003 |
| 4 | `@ResponseBody`로 반환값이 응답본문이 되도록 | 2-02 | 004 |
| 5 | H2 추가, **dev / test 프로파일 분리** | 2-03 | 005 |
| 6 | JPA 추가, **SQL 로그 출력 설정** | 2-03 | 006 |
| 7 | Question, Answer 엔티티 추가 (@ManyToOne은 FK 제약을 건다) | 2-04 | 007 |
| 8 | 특정 질문의 답변 조회 2가지 방법(객체지향: `question.getAnswers()` / 관계지향: `answerRepository.findByQuestion`) | 2-04 | - |
| 9 | OneToMany vs ManyToOne 비교표, cascade REMOVE 이유 | 2-04 | 009 |
| 10 | 패러다임 불일치, JPA의 역할 | 2-04 | - |
| 11 | mappedBy 제거 시 중간테이블 생성됨을 확인 | 2-04 | 011 |
| 12 | mappedBy 추가 시 중간테이블 없이 answer.question_id 사용 | 2-04 | 012 |
| 13 | OneToMany는 유용하면 쓰되 N이 천 단위를 넘는 케이스면 다른 방법(케이스 1~7 예시) | 2-04 | - |
| 14 | 리포지터리 추가, **TestInitData 클래스로 샘플데이터 생성**, findAll 테스트 | 2-05 | 014 |
| 15 | SbbApplicationTests → **QuestionRepositoryTest로 이름 변경**, findById 테스트 | 2-05 | 015 |
| 16 | JPA 메서드 명명규칙 findBySubject, findBySubjectAndContent | 2-05 | 016 |
| 17 | Or/Between/LessThan/GreaterThanEqual/Like/In/OrderBy (SQL 대응표) | 2-05 | 017 |
| 18 | 데이터 수정, **@Transactional로 롤백 → 테스트 간 독립성** (018-1 실패 / 018-2 성공) | 2-05 | 018-1, 018-2 |
| 19 | 데이터 삭제 | 2-05 | 019 |
| 20 | 답변 생성 by answerRepository | 2-05 | 020 |
| 21 | **답변 생성 by oneToMany** (`question.addAnswer`, `CascadeType.PERSIST`, 더티체킹, flush) | 2-05 | 021 |
| 22 | OneToMany에 EAGER 옵션 실험 (SQL 비교) | 2-05 | 022 |
| 23 | `answers = new ArrayList<>()` 초기화 이유, PersistentBag | 2-05 | - |
| 24 | EAGER보다 LAZY. 웬만하면 LAZY만 | 2-05 | - |
| 25 | 테스트 클래스 @Transactional로 LAZY 정상 동작, **@ManyToOne에 fetch=LAZY 필수** | 2-05 | 025 |
| 26 | 도메인별로 분류 (question / answer 패키지) | 2-06 | 026 |
| 27 | 타임리프 없이 `/question/list` 구현 (텍스트 블록 + `@ResponseBody`) | 2-07 | 027 |
| 28 | 타임리프 도입, Model로 변수 전달 | 2-07 | 028 |
| 29 | 타임리프 캐시는 **개발환경에서만** 끄기, IntelliJ inspection의 XML unbound namespace prefix 끄기 | 2-07 | - |
| 30 | questionList를 모델로 전달 | 2-07 | 030 |
| 31 | th:text 문법 | 2-07 | 031 |
| 32 | HTML 테이블로 질문 목록 출력 | 2-07 | 032 |
| 33 | 루트 URL 사용하기 (`/` → redirect) | 2-08 | 033 |
| 34 | 서비스 활용하기 | 2-09 | 034 |
| 35 | 상세 페이지 (1단계 조회/표시, 2단계 목록 제목 링크, 3단계 없는 질문 404) — `@{}` vs `|...|` 비교, 커스텀 예외 이유 | 2-10 | 035-1~3 (태그 `035` = `035-3`과 동일 커밋) |
| 36 | URL 프리픽스 (`@RequestMapping("/question")`) | 2-11 | 036 |
| 37 | 답변 기능 — **강사 커스터마이징: oneToMany로 답변 생성**, RESTful URL 좋은 예/나쁜 예 표 | 2-12 | 037 |
| 38 | 웹 페이지 디자인 (static/style.css) | 2-13 | 038 |
| 39 | 부트스트랩 — **강사 커스터마이징: Pretendard 폰트, cdnjs CDN 사용** | 2-14 | 039 |
| (예고) | 2-15 표준 HTML 구조 ~ 3-13 검색 기능 | - | 미구현 |

도전과제: 책 "3-14 SBB 추가 기능 구현하기" 링크.

## 3. 커밋 메시지 전체 (오래된 순, 35개)
메시지는 모두 강의 번호 한 줄뿐. 괄호는 변경 파일 요약(본인이 `git log --stat`로 확인).
```
20179d9 002    프로젝트 생성 (build.gradle.kts, SbbApplication, application.yaml, SbbApplicationTests)
b80d424 003    MainController 추가
2ee9806 004    MainController @ResponseBody
830ff96 005    H2, application-dev.yaml / application-test.yaml, .gitignore db_dev.*, 테스트에 @ActiveProfiles("test")
34396d2 006    data-jpa, application.yaml JPA/SQL 로그 설정
ef15aa4 007    Question.java, Answer.java
a5cc9b5 009    Question (OneToMany)
a256bd8 011    Question (mappedBy 제거 실험)
27662e4 012    Question (mappedBy 복구)
4132736 014    QuestionRepository, TestInitData, 테스트 findAll
ee1bb94 015    SbbApplicationTests -> QuestionRepositoryTest
2af81a6 016    findBySubject, findBySubjectAndContent
56f1d28 017    findBySubjectLike
740d5a6 018-1  수정 테스트(@Transactional 없음, 실패)
a92bb52 018-2  수정 테스트 @Transactional 추가(성공)
6ffae55 019    삭제 테스트
9f29a85 020    AnswerRepository, 답변 생성 테스트
e10404c 021    Question.addAnswer, cascade PERSIST, answers = new ArrayList<>()
f5806ae 022    OneToMany fetch=EAGER, 답변 조회 테스트
8ca5401 025    EAGER 제거, Answer @ManyToOne(fetch=LAZY), 테스트 클래스 레벨 @Transactional
1e213b5 026    question/ answer/ 패키지로 이동
91c586c 027    QuestionController(@ResponseBody 문자열 HTML), TestInitData @Profile("test") 제거
19dda12 028    Thymeleaf, question_list.html, dev thymeleaf.cache=false, build 의존성 주석 정리
131ac4e 030    questionList 모델 전달 (커밋 날짜가 2025-06-18로 찍혀 있음 - 날짜 이상, 순서상 028과 031 사이)
9267f47 031    th:text
812ac40 032    테이블 출력
4083fa0 033    MainController "/" -> redirect:/question/list
3b89ed0 034    QuestionService
0de5b7c 035-1  detail 액션, question_detail.html
668be50 035-2  목록 제목 링크
0c78bb5 035-3  DataNotFoundException (404)
1b0d46f 036    @RequestMapping("/question")
31c8117 037    AnswerController (question.addAnswer 이용), 상세 화면 답변 목록/폼
cd92c31 038    static/style.css
849f363 039    Bootstrap(cdnjs) + Pretendard, 템플릿 리디자인
```
- 커밋 날짜: 002~028은 2026-05-07~08, 031~039는 2026-11-04~05 (중간 공백). 030만 2025-06-18.
- 008, 010, 013, 023, 024, 029 강은 커밋 없음(설명 강의).

## 4. 최종 패키지 / 파일 구조 (HEAD = 039)
```
build.gradle.kts / settings.gradle.kts / .gitattributes / .gitignore / gradlew(.bat) / gradle/wrapper/*
src/main/java/com/mysite/sbb/
  SbbApplication.java
  MainController.java              // GET "/" -> redirect:/question/list
  DataNotFoundException.java       // @ResponseStatus(NOT_FOUND)
  TestInitData.java                // 샘플데이터 ApplicationRunner (@Configuration)
  question/
    Question.java                  // 엔티티 + addAnswer()
    QuestionRepository.java
    QuestionService.java
    QuestionController.java        // /question/list, /question/detail/{id}
  answer/
    Answer.java
    AnswerRepository.java
    AnswerController.java          // POST /answer/create/{id}
src/main/resources/
  application.yaml / application-dev.yaml / application-test.yaml
  static/style.css
  templates/question_list.html / question_detail.html
src/test/java/com/mysite/sbb/question/QuestionRepositoryTest.java
```
- 도메인별 1단계 패키지(책 2-06과 동일 방식). entity/controller/service 하위 패키지 분리 없음. `global/` 패키지 없음.
- README 없음.

## 5. 책 대비 개선/변경 사항 (실제로 확인된 것만)

### 5-1. dev / test 프로파일 분리 (5강, 커밋 005)
- 책: `application.properties` 하나에 H2 파일 DB(`jdbc:h2:~/local`) 설정.
- 강사: yaml + `spring.profiles.active: dev`. dev는 프로젝트 폴더 파일 DB `jdbc:h2:./db_dev`, test는 메모리 DB `jdbc:h2:mem:db_test`. 테스트 클래스에 `@ActiveProfiles("test")`.
- 이유: 강의 제목 "h2 추가, dev와 test 프로파일 분리" 외 텍스트 설명 없음(영상에서 설명된 것으로 추정 — 미확인). 결과적으로 테스트가 개발 DB를 오염시키지 않고 매번 깨끗한 메모리 DB에서 실행됨.
- DB 파일은 `.gitignore`에 추가.

### 5-2. SQL 로그 상세 출력 (6강, 커밋 006)
- 책: `spring.jpa.properties.hibernate.show_sql/format_sql` 정도.
- 강사: `show-sql`, `format_sql`, `highlight_sql`, `use_sql_comments`, 바인딩 파라미터(`org.hibernate.orm.jdbc.bind: TRACE`), 결과 추출(`extract: TRACE`), 트랜잭션 인터셉터 로그(`org.springframework.transaction.interceptor: TRACE`), ANSI 컬러 `always`. (위 1절 yaml 참고)
- 이유: 강의 텍스트 설명 없음. 이후 강의(21, 22강)에서 SQL 로그로 INSERT/EAGER 조인을 직접 확인하는 학습에 활용.

### 5-3. TestInitData — 테스트용 샘플 데이터를 코드로 초기화 (14강, 커밋 014 / 027)
- 책: 테스트 메서드 안에서 `questionRepository.save(...)`로 데이터를 넣는 방식(테스트 실행 순서/중복 실행에 취약).
- 강사: `@Configuration` + `ApplicationRunner` 빈으로 앱 시작 시 데이터 생성. `@Transactional` 프록시가 적용되도록 **`@Lazy` self 주입**으로 `self.work1()` 호출. `count() > 0`이면 스킵(멱등).
- 처음(014)에는 `@Profile("test")`였으나 027에서 제거 → dev에서도 동작(목록 화면에 샘플이 보이도록 한 것으로 보임 — 이유 텍스트 없음).
```java
@Configuration
@RequiredArgsConstructor
public class TestInitData {
    @Autowired
    @Lazy
    private TestInitData self;
    private final QuestionRepository questionRepository;

    @Bean
    public ApplicationRunner testInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };
    }

    @Transactional
    void work1() {
        if (questionRepository.count() > 0) return;
        Question q1 = new Question();
        q1.setSubject("sbb가 무엇인가요?");
        ...
        q2.addAnswer("네 자동으로 생성됩니다.");
        q2.addAnswer("맞습니다!");
        questionRepository.save(q2);
    }
}
```
(`src/main/java/com/mysite/sbb/TestInitData.java`)

### 5-4. 테스트 클래스 이름/스타일 개선 (15·18·25강)
- 책: `SbbApplicationTests` 하나에 `testJpa()` 반복 수정, JUnit `assertEquals`.
- 강사: `QuestionRepositoryTest`로 대상별 클래스, `@DisplayName("findAll")` + 메서드명 `t1()…t11()`, **AssertJ `assertThat`**, 클래스 레벨 `@ActiveProfiles("test") @SpringBootTest @Transactional`.
- 18강 이유(강의 제목): "@Transactional 어노테이션으로 롤백되도록하여 테스트간 독립성 확보". 018-1에서 수정 테스트를 `t0`(먼저 실행되도록)로 두어 다른 테스트가 깨지는 것을 보여주고, 018-2에서 `@Transactional` 추가로 해결.
- 25강 이유: "@Transactional 어노테이션으로 DB 커넥션이 테스트 기간 중에 유지되도록하여 LAZY 정상작동" → 메서드별 @Transactional을 클래스 레벨로 올림.

### 5-5. OneToMany 필드 활용 + 엔티티 내부 메서드 `addAnswer` (21강 커밋 021, 37강 커밋 037)
- 책: `Answer` 객체 생성 → setter → `answerRepository.save(answer)` (2-05), 2-12에서 `AnswerService.create(question, content)`로 저장.
- 강사: `Question`에 `addAnswer(String content)`를 만들고 `cascade = {PERSIST, REMOVE}` + 컬렉션 초기화. 컨트롤러 트랜잭션 안에서 `question.addAnswer(content)`만 호출하면 더티체킹 + PERSIST로 자동 INSERT. **AnswerService 자체가 없음**, `answerRepository.save` 미사용.
- 이유(강의): OneToMany 필드를 쓰면 "answerRepository를 주입받아야 한다. 귀찮다"를 피하고 "조금 더 객체지향적인 방식". 개념정리: 더티체킹=자동 UPDATE, REMOVE=자동 DELETE, PERSIST=자동 INSERT.
```java
@OneToMany(mappedBy = "question", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
private List<Answer> answers = new ArrayList<>();

public Answer addAnswer(String content) {
    Answer answer = new Answer();
    answer.setContent(content);
    answer.setQuestion(this);
    answer.setCreateDate(LocalDateTime.now());
    answers.add(answer);
    return answer;
}
```
```java
// src/main/java/com/mysite/sbb/answer/AnswerController.java
@PostMapping("/create/{id}")
@Transactional
public String createAnswer(@PathVariable Integer id, @RequestParam("content") String content) {
    Question question = questionService.getQuestion(id);
    question.addAnswer(content);
    return String.format("redirect:/question/detail/%s", id);
}
```
- 주의: `@Transactional`이 **컨트롤러 메서드**에 붙어 있음(서비스 계층 아님). 엔티티에는 여전히 `@Setter`가 열려 있어 캡슐화는 부분적. modify 메서드 등은 아직 없음.

### 5-6. 컬렉션 필드 초기화 (23강)
- 책: `private List<Answer> answerList;` (초기화 없음, 필드명 `answerList`).
- 강사: `private List<Answer> answers = new ArrayList<>();` (필드명도 `answers`).
- 이유(강의): NullPointerException 방지, 생성 직후 바로 사용 가능. JPA가 로딩한 엔티티는 `PersistentBag`을 쓰므로 초기값은 새 객체에만 쓰인다.

### 5-7. 지연 로딩(LAZY) 원칙 (22·24·25강, 커밋 022/025)
- 책: fetch 옵션 언급 없음(@ManyToOne 기본 EAGER).
- 강사: 022에서 OneToMany EAGER 실험(LEFT JOIN SQL 확인) 후 025에서 제거, `Answer`의 `@ManyToOne(fetch = LAZY)` 적용(static import 사용).
- 이유(강의): "EAGER는 불필요한 데이터까지 가져와 성능 저하, 메모리 사용 증가. LAZY는 필요할 때만 로딩." "@ManyToOne에는 꼭 fetch=LAZY 옵션 걸어주세요. 기본값은 EAGER."
```java
@ManyToOne(fetch = LAZY)
private Question question;
```

### 5-8. mappedBy 유무 실험 (11·12강)
- 책 대비 추가 설명: mappedBy를 빼면 JPA가 중간테이블을 만든다 → 넣으면 answer.question_id 사용. 이유: "JPA에 요청할 때도 엔티티와 테이블의 관계를 명확하게 정의해야 한다."

### 5-9. OneToMany 사용 기준 (13강, 설명만)
- "유용하다면 일단 사용, N이 천을 넘는 케이스가 많아지면 다른 방법." 예: 글-댓글 O, 대형 서비스 댓글 X(페이징 필요), 대형 서비스 좋아요 X(개수 캐싱).

### 5-10. 엔티티 세부 스타일
- `@Id @GeneratedValue(strategy = IDENTITY) private int id;` — 책은 `Integer`, 강사는 **primitive `int`** (static import `GenerationType.IDENTITY`). 이유 설명 없음. 그 결과 새 Answer의 id는 저장 전 `0`(테스트 t9에서 `isEqualTo(0)` 확인).
- `@Setter @Getter` 순서로 붙임(책과 같은 Lombok 사용).

### 5-11. 타임리프 전 단계: 텍스트 블록으로 HTML 직접 응답 (27강, 커밋 027)
- 책에 없는 단계. Java 텍스트 블록 + `formatted()` + stream으로 `<li>` 생성 후 `@ResponseBody` 반환 → 다음 강(28)에서 템플릿 엔진의 필요성을 보여줌.

### 5-12. 타임리프 캐시는 dev에서만 끄기 (28·29강)
- `application-dev.yaml`에만 `spring.thymeleaf.cache: false`. 이유(강의 제목): "타임리프 캐시는 오직 개발환경에서만 꺼야 합니다."

### 5-13. 서비스 / 예외 (34·35강)
- 서비스는 생성자 주입(`@RequiredArgsConstructor` + `private final`) — 책과 동일 방식(책도 RequiredArgsConstructor 사용).
- 404: `DataNotFoundException` + `@ResponseStatus(NOT_FOUND, reason = "entity not found")` — 책과 동일. 강의에서 이유 설명 추가: "자바 기본 예외(`No value present`)는 추상적이고 불친절, 구체적 예외가 긴급상황에 문제 파악에 도움. 무조건 만들 필요는 없다."
- 상세 액션 파라미터는 `@PathVariable int id` (책은 `Integer id`).

### 5-14. URL 설계 (37강, 설명만 — 코드는 책 URL 유지)
- 코드: `POST /answer/create/{id}`, `GET /question/list`, `GET /question/detail/{id}` (책과 동일).
- 강의 표: 좋은 예 `POST /questions/5/answers`, `GET /answers/5`, `PUT /answers/5`, `GET /questions`, `DELETE /questions/5`. "자원은 명사(복수형), 행위는 HTTP Method. 다만 HTML form은 GET/POST만 지원해 POST로 표현하는 경우가 많다."

### 5-15. 프론트 리소스: CDN 사용 + Pretendard 폰트 (39강)
- 책: bootstrap.min.css를 다운로드해 `static/`에 넣음.
- 강사: `https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.8/css/bootstrap.min.css` 링크, `static/style.css`에 Pretendard `@font-face`(jsdelivr). 이유(강의): "프론트엔드 라이브러리는 백엔드 라이브러리와 달리 쉽게 가져다 쓸 수 있는 경우가 많다."
- 템플릿에서 날짜는 `#temporals.format(..., 'yyyy-MM-dd HH:mm')`, 번호는 `loop.count`. 아직 `<html>/<head>` 없는 조각 HTML(책 2-14 시점과 동일; 2-15에서 표준 구조 예정).

### 5-16. 빌드 / 기타
- Gradle **Kotlin DSL**, Java 25, Spring Boot 4.1.2 최신 스택(책은 Groovy DSL, Java 17 / Boot 3.x).
- `.gitattributes`로 gradlew LF / bat CRLF / jar binary 지정(이니셜라이저 기본값으로 보임).
- 33강: `/`는 `redirect:/question/list` (책 2-08과 동일).

## 6. 테스트 방식
파일: `src/test/java/com/mysite/sbb/question/QuestionRepositoryTest.java` (유일한 테스트)
```java
@ActiveProfiles("test")
@SpringBootTest
@Transactional
class QuestionRepositoryTest {
    @Autowired private QuestionRepository questionRepository;
    @Autowired private AnswerRepository answerRepository;

    @Test
    @DisplayName("findAll")
    void t1() {
        List<Question> questions = questionRepository.findAll();
        assertThat(questions).hasSize(2);
        ...
    }
}
```
- 통합 테스트(`@SpringBootTest`) + 메모리 H2(test 프로파일) + 데이터는 `TestInitData`가 앱 기동 시 생성(질문 2, 답변 2).
- 클래스 레벨 `@Transactional`로 각 테스트 롤백 → 독립성 + LAZY 로딩 가능.
- 테스트 목록: t1 findAll, t2 findById, t3 findBySubject, t4 findBySubjectAndContent, t5 findBySubjectLike, t6 수정, t7 삭제, t8 답변 생성(answerRepository), t9 답변 생성 by oneToMany, t10 답변 조회, t11 답변 조회 by oneToMany.
- 필드 주입(`@Autowired`) 사용(테스트에서는 허용).
- 강의 21강 텍스트: 롤백되는 테스트에서 INSERT 로그를 보려면 `questionRepository.flush()` 추가(최종 코드엔 flush 없음).
- MockMvc / 컨트롤러 테스트 / 서비스 단위 테스트는 **없음**.

## 7. 커밋 / README 관례
- 커밋 메시지 = **강의 번호만** (`002`, `018-1`, `035-2`). 한 강의에 여러 단계면 `-1`, `-2`, `-3` 접미사. 동일 이름의 **git tag**를 달아 강의 페이지에서 `github.com/jhs512/p-14163-2/commit/035-1`처럼 태그로 링크.
- 설명 없는 강의(8, 10, 13, 23, 24, 29)는 커밋 없음 → 번호가 건너뜀.
- README 없음. 커밋 본문 없음.
- 강의 페이지 포맷: 강마다 `# N강` + 유튜브 링크 + 커밋 링크(구/신규) + 책 wikidocs 챕터 링크 + 보충 설명, 강사 변경점은 `## 강사의 커스터마이징` 섹션으로 표시.

## 8. 학습 프로젝트에 적용할 때 요약
1. Boot 4.x + Java 25 + Gradle Kotlin DSL + yaml.
2. `application.yaml`(공통, active=dev, SQL 로그) / `application-dev.yaml`(파일 H2, thymeleaf cache off) / `application-test.yaml`(메모리 H2).
3. `TestInitData`(ApplicationRunner + @Lazy self + @Transactional + count 체크)로 샘플데이터.
4. 엔티티: `answers = new ArrayList<>()`, `cascade {PERSIST, REMOVE}`, `@ManyToOne(fetch = LAZY)`, `question.addAnswer(content)`로 답변 생성(AnswerService 불필요).
5. 테스트: 대상별 클래스, `@ActiveProfiles("test") @SpringBootTest @Transactional`, `@DisplayName` + `tN()`, AssertJ.
6. 커밋 메시지는 단계 번호 + 태그.
7. Bootstrap은 cdnjs CDN, Pretendard 폰트.
(BaseEntity, Auditing, RsData, Security 등은 이 강의 범위에서 아직 등장하지 않음)
