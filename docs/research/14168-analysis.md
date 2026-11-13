# 14168 "JPA, 타임리프 개선" 분석 보고서

- 강의: https://www.slog.gg/p/14168/raw (63KB, 전체 확인)
- 저장소: https://github.com/jhs512/p-14168-2 (커밋 95개, HEAD 파일 30개 전부 확인)
- 참고: 강의 원본 저장소는 `p-13969-1`. `p-14168-1`, `p-14168-2`는 2026-11-10 기준 최신 환경으로 재작성된 동일 이력이며, 커밋 메시지가 곧 강의 태그 번호(`001`, `009-1` ...)다. 각 강의 페이지의 "신규" 링크는 `https://github.com/jhs512/p-14168-2/commit/<태그>` 형식.
- 표기: [확인] = 저장소/강의에서 직접 확인, [교재-기억] = 점프 투 스프링부트(sbb3) 쪽 내용은 이번에 원문을 가져오지 않았으므로 기억 기반이며 재확인 필요.

---

## 1. 기술 스택 / 버전 [확인]

| 항목 | 값 | 출처 |
|---|---|---|
| Java | 25 (toolchain) | `build.gradle.kts` |
| Spring Boot | 4.1.2 | `build.gradle.kts` |
| dependency-management | 1.1.7 | `build.gradle.kts` |
| 빌드 | Gradle 9.8.2, Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts` rootProject = `back`) | `gradle/wrapper/gradle-wrapper.properties` |
| 패키지 루트 | `com.back` (group `com`) | |
| DB | H2. dev: `jdbc:h2:./db_dev;MODE=MySQL` (파일, AUTO_SERVER 미사용), test: `jdbc:h2:mem:db_test;MODE=MySQL` | `application-dev.yaml`, `application-test.yaml` |
| H2 콘솔 | `spring-boot-h2console` 모듈 (Boot 4에서 분리된 모듈) | |
| 웹 | `spring-boot-starter-webmvc` (Boot 4 명칭) | |
| JPA | `spring-boot-starter-data-jpa` | |
| 템플릿 | `spring-boot-starter-thymeleaf` + `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect` (44강에서 추가) | |
| 검증 | `spring-boot-starter-validation` | |
| 보안 | `spring-boot-starter-security` (46강에서 추가) | |
| Lombok | compileOnly + annotationProcessor (test도 동일) | |
| devtools | developmentOnly | |
| 테스트 | `spring-boot-starter-{data-jpa,security,thymeleaf,validation,webmvc}-test`, junit-platform-launcher | |
| CSS 프레임워크 | 없음. layout.html에 jQuery 3.7.1 / Bootstrap 5.3.8 CDN이 **주석 처리**된 채로만 존재 | `templates/layout.html` |
| p6spy / QueryDSL / Thymeleaf extras-springsecurity | **사용하지 않음** | |

### application.yaml 전체 설정 [확인]
```yaml
spring:
  application.name: back
  profiles.active: dev
  output.ansi.enabled: always
  mvc.hiddenmethod.filter.enabled: true      # 41강: _method 로 PUT/DELETE
  datasource.hikari.auto-commit: false
  jpa:
    show-sql: true
    hibernate.ddl-auto: update
    properties.hibernate:
      format_sql: true
      highlight_sql: true
      use_sql_comments: true
logging.level:
  org.hibernate.orm.jdbc.bind: TRACE         # 바인딩 파라미터 로그
  org.hibernate.orm.jdbc.extract: TRACE
  org.springframework.transaction.interceptor: TRACE   # 트랜잭션 시작/종료 로그
```
`application-dev.yaml`: `spring.thymeleaf.cache: false`, `spring.thymeleaf.prefix: file:src/main/resources/templates/` (재시작 없이 템플릿 반영, 18강).
- `spring.jpa.open-in-view`는 **설정 없음**(기본값 true, 경고 로그 발생). 대신 컨트롤러 메서드에 `@Transactional`을 직접 붙이는 방식(아래 5장).
- `.gitignore`에 `db_dev.mv.db`, `db_dev.trace.db`, `db_dev.lock.db` 추가 (강의 2강 설명은 `db_test.mv.db`도 언급하나 HEAD에는 dev 3종만 있음).

---

## 2. 강의 구조 (강 단위) [확인]

| 강 | 제목/내용 | 커밋 |
|---|---|---|
| 1 | 프로젝트 세팅 (IntelliJ로 생성, git 연결) | 001 |
| 2 | 샘플 데이터 3건 자동 생성: `BaseInitData`(@Configuration) + `ApplicationRunner` 빈 → `PostService` → `PostRepository` → `Post` → `BaseEntity`(id, createDate, modifyDate, Auditing) → `@EnableJpaAuditing` → yml(ddl-auto update, dev 파일DB, test 메모리DB) → .gitignore. "사용하려는 것부터 쓰고 없는 것을 만들어 나가는" top-down 순서 | 002 |
| 3 | 폼은 URL 생성기에 불과하다 (CodePen) | - |
| 4 | 글 작성 폼 (`/posts/write` → action `/posts/doWrite?title=..&content=..`) | 004 |
| 5 | action 주소에서 프로토콜/도메인/포트 생략, 겹치는 path 생략 | 005-1, 005-2 |
| 6 | `/posts/doWrite` 액션 메서드로 글 작성 | 006 |
| 7 | 폼 전송은 GET이 아니라 POST, HTTP 메서드/멱등성 표 | 007 |
| 8 | 멱등성은 참고만 | - |
| 9 | 유효성 체크 수동 구현, 실패 시 폼 재표시, `getWriteFormHtml`로 중복 제거 (이 시점은 Java 문자열로 HTML 생성) | 009-1~4 |
| 10 | 재표시 시 입력값 유지, `autofocus`, 실패 필드 포커스 | 010-1~3 |
| 11 | Validation 의존성, `@Validated` + 파라미터 `@NotBlank`, `@Size` (제목 2~20, 내용 2~100) | 011-1, 011-2 |
| 12 | 입력값을 객체로 받기 = `@ModelAttribute` + `@Valid` (→ `@Validated` 제거, `@RequestParam` 기본값 제거) | 012 |
| 13 | `BindingResult`로 예외 대신 커스터마이징, 대표 에러 출력, 커스텀 메시지 | 013-1~3 |
| 14 | `getFieldErrors()` + 스트림으로 모든 에러 출력, 메시지 앞에 숫자로 순서 지정 | 014-1, 014-2 |
| 15 | 메시지에 필드명 추가 → `<li data-error-field-name>` → 정렬 후 첫 에러 필드 focus (프론트 JS) | 015-1~3 |
| 16 | 타임리프 도입: write.html, writeDone.html, th:text vs th:utext, `writeForm?.title` vs `writeForm.title`, GET 핸들러에도 `WriteForm form` 파라미터(단 `@Valid`는 제거) | 016-1~8 |
| 17 | 타임리프 도입 복기 (영상만) | - |
| 18 | 타임리프 캐시 끄기, `@ModelAttribute("form")`으로 템플릿 변수명 `form` 통일 | 018 |
| 19 | 컨트롤러 내 `@ModelAttribute("siteName")` 메서드 → 템플릿 공통 변수 (전역은 `@ControllerAdvice` 예시) | 019 |
| 20 | `th:block` | 020 |
| 21 | label `for`/`id` 규칙 (정석/보편/현대적 방식) | 021 |
| 22 | `th:object` + `th:field`로 name/id/value 자동 | 022-1, 022-2 |
| 23 | 직접 만든 errorMessage 대신 `#fields`로 에러 출력, 정렬 | 023-1, 023-2 |
| 24 | 메시지 형식 `NN-필드명-메시지`, `th:data-error-field-name`, `th:with`로 split 결과 변수화 | 024-1~5 |
| 25 | focus JS 복원, `setTimeout`으로 지연, 에러 ul+script를 **프래그먼트**(`form_errors.html`)로 분리 | 025-1~6 |
| 26 | `POST /posts/doWrite` → `POST /posts/write`, action이 현재 URL이면 생략 | 026-1, 026-2 |
| 27 | meta refresh 3초 후 리다이렉트 | 027 |
| 28 | meta refresh 대신 액션 메서드에서 302(`redirect:`) | 028-1, 028-2 |
| 29 | 상세페이지, `white-space: pre-line`, 링크, 목록은 일단 `@ResponseBody List<Post>`(JSON) | 029-1~5 |
| 30 | Jackson은 public 필드/getter만 노출, getter 접근레벨 조절 후 원복 | 030-1, 030-2 |
| 31 | list.html (SSR), 작성/수정일 표시 (`#temporals.format`); 강의 페이지에 fetch/React CSR 비교 예시 | 031-1, 031-2 |
| 32 | 글 수정 (modify.html, 처리 후 상세로) | 032-1, 032-2 |
| 33 | `PostComment` 엔티티 + 샘플 댓글. 1차 실패 → `self` + `@Transactional`로 성공 | 033-1, 033-2 |
| 34 | 댓글 목록 | 034 |
| 35 | 댓글 작성 폼 + 클라이언트 JS 검증(`onsubmit="...; return false;"` → `form.submit()`) | 035-1~4 |
| 36 | 댓글 작성 처리 (서버 검증은 단순화) | 036 |
| 37 | 댓글 삭제: orphanRemoval 없어서 실패 → 추가로 성공, `confirm()`, BaseEntity equals/hashCode | 037-1~5 |
| 38 | equals에 동일성(`==`) 비교 추가 | 038 |
| 39 | 댓글 수정 + JS 검증 | 039-1, 039-2 |
| 40 | 폼 DTO를 `record`로 | 040 |
| 41 | HiddenHttpMethodFilter 활성화, `_method=PUT` → `@PutMapping` | 041 |
| 42 | `select[value]` 기본 선택 JS (CodePen) | - |
| 43 | `a[method="DELETE"]`로 POST/PUT/DELETE 보내는 JS (CodePen) | - |
| 44 | layout dialect 도입, `layout.html`에서 `global.js` 전역 적용, siteName 제거, 댓글 삭제 `@DeleteMapping` | 044-1~3 |
| 45 | `layout:title-pattern`으로 페이지별 title | 045 |
| 46 | Spring Security 도입(전부 막힘) → 전체 허용, 정적 파일 `/static/resource/`로 이동, CSRF hidden input → `th:action`으로 자동 처리, `a[method]` DELETE용 `<meta name="_csrf">` | 046-1~5 |
| 47 | 미션 안내 (점프 투 스프링부트 + 14163/14168 적용, 커밋/README 규칙) | - |

---

## 3. 커밋 메시지 전체 (시간순, 95개) [확인]

커밋 메시지는 **번호만** 있다(본문 없음). 의미는 강의 페이지의 링크 설명에 있다.

```
001
002
004
005-1 005-2
006
007
009-1 009-2 009-3 009-4
010-1 010-2 010-3
011-1 011-2
012
013-1 013-2 013-3
014-1 014-2
015-1 015-2 015-3
016-1 016-2 016-3 016-4 016-5 016-6 016-7 016-8
018
019
020
021
022-1 022-2
023-1 023-2
024-1 024-2 024-3 024-4 024-5
025-1 025-2 025-3 025-4 025-5 025-6
026-1 026-2
027
028-1 028-2
029-1 029-2 029-3 029-4 029-5
030-1 030-2
031-1 031-2
032-1 032-2
033-1 033-2
034
035-1 035-2 035-3 035-4
036
037-1 037-2 037-3 037-4 037-5
038
039-1 039-2
040
041
044-1 044-2 044-3
045
046-1 046-2 046-3 046-4 046-5
```
(003, 008, 017, 042, 043은 코드 변경 없는 강의라 커밋 없음. 강별 의미는 2장 표 참조.)

---

## 4. 최종 패키지/파일 구조 (HEAD) [확인]

```
build.gradle.kts / settings.gradle.kts / gradlew(.bat) / gradle/wrapper/*
.gitignore / .gitattributes
src/main/java/com/back/
├── BackApplication.java                      (@SpringBootApplication @EnableJpaAuditing)
├── domain/post/
│   ├── post/
│   │   ├── controller/PostController.java    (WriteForm/ModifyForm record 내장)
│   │   ├── entity/Post.java
│   │   ├── repository/PostRepository.java
│   │   └── service/PostService.java
│   └── postComment/
│       ├── controller/PostCommentController.java (WriteForm/ModifyForm record 내장)
│       └── entity/PostComment.java          (repository/service 없음 → Post 애그리거트 경유)
└── global/
    ├── initData/BaseInitData.java
    ├── jpa/entity/BaseEntity.java
    └── security/SecurityConfig.java
src/main/resources/
├── application.yaml / application-dev.yaml / application-test.yaml
├── static/resource/global.js
└── templates/
    ├── layout.html
    ├── form_errors.html
    └── post/
        ├── post/{list,detail,write,modify}.html
        └── postComment/modify.html
src/test/java/com/back/BackApplicationTests.java
```
패키지 규칙: `domain/<도메인>/<엔티티>/{controller,entity,repository,service}` + `global/<관심사>`. 템플릿 경로도 `templates/<도메인>/<엔티티>/`로 미러링.
README.md는 저장소에 **없음**.

---

## 5. JPA 개선 사항

### 5-1. BaseEntity (@MappedSuperclass + Auditing) [확인]
- 교재 [교재-기억]: `Question`, `Answer` 각각에 `@Id @GeneratedValue(strategy = IDENTITY) Integer id`, `LocalDateTime createDate`를 두고 서비스에서 `setCreateDate(LocalDateTime.now())`로 수동 세팅, 수정일(`modifyDate`)도 나중에 수동 세팅.
- 강사: 공통 필드를 `BaseEntity`로 올리고 Spring Data Auditing으로 자동 기록. id는 `int`(primitive).
- 이유: 중복 제거, 날짜 세팅 누락 방지, 엔티티는 비즈니스 필드만.
```java
// src/main/java/com/back/global/jpa/entity/BaseEntity.java
@MappedSuperclass // 엔티티의 부모 클래스에는 이걸 달아야 한다.
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    @Id @GeneratedValue(strategy = IDENTITY)
    private int id;
    @CreatedDate      private LocalDateTime createDate;
    @LastModifiedDate private LocalDateTime modifyDate;
    ...
}
// BackApplication.java
@SpringBootApplication
@EnableJpaAuditing
```

### 5-2. BaseEntity equals / hashCode (37-5, 38강) [확인]
- 교재: 없음 [교재-기억].
- 강사: id 기반 동등성. 38강에서 `o == this` 동일성 체크를 먼저 두어 빠른 비교.
- 이유: `comments.remove(postComment)`처럼 컬렉션에서 엔티티를 찾아 지울 때 "ID가 같으면 같은 댓글"로 인식하게 하기 위해. hashCode는 HashSet/HashMap용이며 equals와 함께 오버라이딩하는 것이 관례.
```java
@Override
public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseEntity that = (BaseEntity) o;
    return id == that.id;
}
@Override
public int hashCode() { return Objects.hashCode(id); }
```
(주의: `getClass()` 비교라 Hibernate 프록시와 비교 시 false가 될 수 있음. 강의에서 이 점은 다루지 않음 — 분석자 의견.)

### 5-3. 엔티티 생성/캡슐화 [확인]
- `@Entity @Getter @NoArgsConstructor`, **@Setter 없음**. 생성은 의미 있는 생성자(`new Post(title, content)`), 변경은 엔티티 메서드(`post.modify(...)`, `postComment.modify(...)`).
- 교재 [교재-기억]: `@Getter @Setter` 엔티티 + 서비스에서 `q.setSubject(...)`, `q.setContent(...)` 후 `repository.save(q)`.
```java
// Post.java
public Post(String title, String content) { this.title = title; this.content = content; }
public void modify(String title, String content) { this.title = title; this.content = content; }
```

### 5-4. 연관관계: 양방향 + mappedBy + cascade + orphanRemoval + LAZY (33, 37강) [확인]
- 교재 [교재-기억]: `Question`에 `@OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) List<Answer> answerList`, `Answer`에 `@ManyToOne Question question`(fetch 기본 EAGER). 답변 저장은 `AnswerService`가 `new Answer()` + setter + `answerRepository.save()`.
- 강사:
```java
// Post.java
@OneToMany(mappedBy = "post", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
private final List<PostComment> comments = new ArrayList<>();

// PostComment.java
@ManyToOne(fetch = LAZY)
private Post post; // POST_ID
```
- 강의 설명 요지:
  - `mappedBy="post"`: 연관관계 주인은 `PostComment.post` (FK `post_id`는 post_comment 테이블).
  - `PERSIST`: Post 저장(**또는 더티체킹**) 시 컬렉션에 추가된 댓글 자동 INSERT → 그래서 `PostCommentRepository`가 필요 없음.
  - `REMOVE`: Post 삭제 시 댓글 먼저 삭제.
  - `orphanRemoval=true`: `post.getComments().remove(comment)`만 해도 커밋 시 DELETE. 037-1에서 이 옵션 없이 삭제 시도 → 실패, 037-2에서 추가 → 성공 (실패를 직접 보여주는 커밋).
  - `@ManyToOne(fetch = LAZY)`: 명시적으로 LAZY (기본 EAGER 회피). 강의 텍스트에 이유 설명은 없음.
- 컬렉션은 `final` + 즉시 초기화.

### 5-5. 애그리거트 루트를 통한 자식 조작 (엔티티 메서드 vs 서비스) [확인]
- 강사는 `PostCommentRepository`/`PostCommentService`를 **만들지 않는다**. 댓글 작성/조회/삭제를 모두 `Post` 엔티티 메서드로 하고, `PostService`는 얇게 위임만 한다.
```java
// Post.java
public PostComment addComment(String content) {
    PostComment postComment = new PostComment(this, content);
    comments.add(postComment);          // 양방향 동기화 + cascade PERSIST
    return postComment;
}
public Optional<PostComment> findCommentById(int id) {
    return comments.stream().filter(c -> c.getId() == id).findFirst();
}
public boolean deleteComment(PostComment postComment) {
    if (postComment == null) return false;
    return comments.remove(postComment); // orphanRemoval + equals
}

// PostService.java (위임)
public void writeComment(Post post, String content) { post.addComment(content); }
public boolean deleteComment(Post post, PostComment c) { return post.deleteComment(c); }
public void modifyComment(PostComment c, String content) { c.modify(content); }
public void modify(Post post, String title, String content) { post.modify(title, content); }
```
- 교재 [교재-기억]: `AnswerRepository`, `AnswerService` 별도, `answerRepository.save/delete` 직접 호출.
- 이유(강의 맥락): 부모-자식 일관성을 엔티티가 책임, save 호출 불필요(더티체킹+cascade).
- 주의(분석자 의견): `findCommentById`는 컬렉션 전체를 로딩 후 스트림 필터 — 댓글이 많으면 비효율. 강의는 다루지 않음.

### 5-6. 더티체킹 (save 호출 없음) [확인]
- `PostService.modify`, `modifyComment`, `writeComment`는 `repository.save()`를 호출하지 않는다. 트랜잭션 안에서 엔티티 변경 → 커밋 시 UPDATE/INSERT.
- `write`만 신규 엔티티라 `postRepository.save(post)`.
- 교재 [교재-기억]: 수정 후에도 `questionRepository.save(question)` 호출.

### 5-7. @Transactional 위치: 컨트롤러 메서드 [확인]
- `PostService`에는 `@Transactional`이 **없다**. 대신 **컨트롤러 액션 메서드**마다 붙인다.
  - 조회: `@Transactional(readOnly = true)` (showDetail, showList, showModify, 댓글 showModify)
  - 변경: `@Transactional` (write, modify, 댓글 write/modify/delete)
  - `showWrite`, `redirectToList`에는 없음.
```java
@GetMapping("/posts/{id}")
@Transactional(readOnly = true)
public String showDetail(@PathVariable int id, Model model) { ... }

@PutMapping("/posts/{id}/modify")
@Transactional
public String modify(...) { ... }
```
- 이유(추론, 강의 텍스트에 명시 없음): 한 요청 = 한 트랜잭션. 컨트롤러에서 `findById` 후 엔티티 메서드 호출 → 더티체킹이 같은 트랜잭션 안에서 일어나게 함. 템플릿 렌더링 중 LAZY 로딩(`post.comments`)은 open-in-view 기본값(true)에 의존하는 것으로 보임 [불확실].
- `application.yaml`의 `logging.level.org.springframework.transaction.interceptor: TRACE`로 트랜잭션 경계를 로그로 확인.
- `hikari.auto-commit: false` 설정 (트랜잭션 밖 자동 커밋 방지 및 성능 목적으로 보임 [추론]).

### 5-8. 초기 데이터: ApplicationRunner + self 프록시 + @Transactional (33강) [확인]
- 033-1: `ApplicationRunner` 람다 안에서 `post1.addComment(...)` → 트랜잭션 밖이라 `post1`이 이미 detached → 댓글 저장 실패 (실패 커밋으로 보여줌).
- 033-2: `@Lazy @Autowired BaseInitData self`로 자기 자신의 **프록시**를 주입받아 `self.work1()` 호출 → `@Transactional`이 적용되어 더티체킹/cascade로 댓글 저장 성공.
- 이유: 같은 클래스 내부 호출(`this.work1()`)은 프록시를 거치지 않아 `@Transactional`이 무시되기 때문.
```java
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired @Lazy
    private BaseInitData self;
    private final PostService postService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> { self.work1(); };
    }

    @Transactional
    public void work1() {
        if (postService.count() > 0) return;   // 중복 생성 방지
        Post post1 = postService.write("제목 1", "내용 1");
        ...
        post1.addComment("댓글 1-1");          // save 없이 cascade로 INSERT
    }
}
```
- 교재 [교재-기억]: 테스트 코드(`@SpringBootTest`)에서 반복문으로 300건 생성.

### 5-9. Repository / 쿼리 [확인]
- `PostRepository extends JpaRepository<Post, Integer>` — **커스텀 쿼리 메서드 없음**.
- 페이징/검색(Specification, QueryDSL, @Query): **이 강의에는 없음**. (교재의 페이징/검색 개선은 14163 강의 쪽을 확인할 필요가 있음.)
- N+1 처리(fetch join, @EntityGraph, `default_batch_fetch_size`): **없음**. 목록(list.html)은 댓글을 쓰지 않아 현재 N+1 문제는 드러나지 않음.

### 5-10. 조회 결과 처리 [확인]
- `findById(id).get()`을 컨트롤러에서 바로 호출 (없는 id 예외 처리 없음). 교재의 `DataNotFoundException` 같은 처리 없음. 개선 여지(분석자 의견).

### 5-11. DDL / SQL 로깅 [확인]
- `ddl-auto: update` (dev/test 공통, application.yaml).
- SQL 로깅은 p6spy 없이 Hibernate 내장: `show-sql`, `format_sql`, `highlight_sql`, `use_sql_comments` + `org.hibernate.orm.jdbc.bind/extract: TRACE`(바인딩 값까지).
- H2 `MODE=MySQL`로 운영 DB 호환 의도.

### 5-12. 엔티티를 JSON으로 직접 반환하는 것에 대한 학습 (29~30강) [확인]
- 29-5에서 `@ResponseBody List<Post>` 반환 → Jackson이 public getter 기준 직렬화. 30-1에서 필드별 getter 접근레벨 조절 실험 후 30-2에서 원복. 최종 코드는 타임리프 방식이라 JSON 엔드포인트 없음.

---

## 6. 타임리프 개선 사항

### 6-1. 레이아웃: Thymeleaf Layout Dialect (44, 45강) [확인]
- 교재 [교재-기억]: 교재도 `layout.html` + `layout:decorate`/`layout:fragment` 방식(템플릿 상속 장)과 `navbar.html` 등 `th:replace` 프래그먼트를 사용. 따라서 "도입 자체"보다는 아래 세부 패턴이 차이점.
- 강사:
```html
<!-- templates/layout.html -->
<title layout:title-pattern="$CONTENT_TITLE | $LAYOUT_TITLE">커뮤니티 사이트 A</title>
<meta name="_csrf" th:content="${_csrf.token}">
<script src="/resource/global.js" defer></script>
...
<th:block layout:fragment="content"></th:block>
```
```html
<!-- templates/post/post/detail.html -->
<html layout:decorate="~{layout}">
<head>
    <title th:text="|${post.id}번 글 본문 - ${post.title}|"></title>
</head>
<div layout:fragment="content"> ... </div>
</html>
```
- `layout:title-pattern`으로 "페이지 제목 | 사이트명" 자동 조합 (45강).
- 레이아웃에서 `global.js` 한 번 로드 → 모든 페이지에서 `a[method]`, `select[value]` 사용 가능.
- 레이아웃에 `<meta name="_csrf">` → JS에서 CSRF 토큰 사용.
- IntelliJ `Namespace 'layout' is not bound` 경고 관련 언급 있음 (자식 템플릿에 xmlns 선언 없음).
- 이전 단계(19강)의 `@ModelAttribute("siteName")`은 레이아웃 도입 후 제거. 전역 모델 속성이 필요하면 `@ControllerAdvice` + `@ModelAttribute` 사용(강의 예시 코드).

### 6-2. 폼 DTO: record + @ModelAttribute("form") + @Valid + BindingResult (12~18, 40강) [확인]
- 교재 [교재-기억]: 별도 파일 `QuestionForm` 클래스(`@Getter @Setter`), 템플릿에서 `th:object="${questionForm}"`, GET 핸들러에 `QuestionForm questionForm` 파라미터.
- 강사: 컨트롤러 내부 `public record WriteForm(...)`, 이름을 `"form"`으로 통일, GET 핸들러에도 같은 타입을 받되 `@Valid`는 붙이지 않음(붙이면 GET에서 검증 실패, 016-7/016-8).
```java
public record WriteForm(
        @NotBlank(message = "01-title-제목을 입력해주세요.")
        @Size(min = 2, max = 20, message = "02-title-제목은 2자 이상, 20자 이하로 입력가능합니다.")
        String title,
        @NotBlank(message = "03-content-내용을 입력해주세요.")
        @Size(min = 2, max = 20, message = "04-content-내용은 2자 이상, 20자 이하로 입력가능합니다.")
        String content
) {}

@GetMapping("/posts/write")
public String showWrite(@ModelAttribute("form") WriteForm form) { return "post/post/write"; }

@PostMapping("/posts/write")
@Transactional
public String write(@ModelAttribute("form") @Valid WriteForm form, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) return "post/post/write";
    Post post = postService.write(form.title, form.content);
    return "redirect:/posts/" + post.getId();
}
```
- 수정 폼은 GET에서 `model.addAttribute("form", new ModifyForm(post.getTitle(), post.getContent()))`로 기존 값 채움.
- record 컴포넌트를 `form.title`(필드 직접 접근, 같은 클래스 내부라 가능)으로 사용.
- 이유: 코드량 감소, 불변 DTO, 템플릿 변수명 통일로 공통 프래그먼트 재사용 가능.
- 참고: 최종 코드에서 PostController의 내용 `@Size`는 max=20 (강의 11강 설명은 내용 2~100자) — 불일치 [확인, 원인 불명].

### 6-3. th:object + th:field (22강) [확인]
```html
<form method="POST" th:action th:object="${form}">
    <div th:replace="~{form_errors :: formErrorsFragment}"></div>
    <label for="title">제목</label><br>
    <input type="text" placeholder="제목" th:field="*{title}" autofocus>
    <textarea placeholder="내용" th:field="*{content}"></textarea>
    <input type="submit" value="작성">
</form>
```
- `th:field`가 name/id/value 자동 생성 → 값 유지(10, 16강의 수동 구현을 대체). `label for` 경고는 IntelliJ에서 `unresolved 'id'` 검사 해제로 처리.

### 6-4. 에러 메시지 표시: 정렬 + 필드명 인코딩 + 첫 에러 필드 포커스 (13~15, 23~25강) [확인]
- 교재 [교재-기억]: `form_errors.html`에서 `#fields.allErrors()`를 `alert alert-danger` div로 단순 나열 (순서 비결정적).
- 강사: 메시지 형식을 `"NN-필드명-메시지"`로 정해 ①`sorted()`로 순서 고정 ②`split('-', 3)`로 필드명/메시지 분리 ③`data-error-field-name`에 필드명 → JS가 첫 에러 필드에 focus.
```html
<!-- templates/form_errors.html -->
<th:block th:fragment="formErrorsFragment">
    <ul th:if="${#fields.hasAnyErrors()}" style="color: red;">
        <li th:each="err : ${#fields.allErrors().stream().sorted()}"
            th:with="errStrs = ${err.split('-', 3)},
                errFieldName = ${errStrs[1]},
                errMsg = ${errStrs[2]}"
            th:data-error-field-name="${errFieldName}"
            th:text="${errMsg}"></li>
    </ul>
    <script>
        const forms = document.querySelectorAll('form');
        const lastForm = forms[forms.length - 1];   // 프래그먼트가 들어간 바로 그 폼
        const errorFieldName = lastForm.querySelector('ul > li')?.dataset?.errorFieldName || '';
        if (errorFieldName.length > 0) {
            setTimeout(() => lastForm[errorFieldName].focus());  // 폼이 다 그려진 뒤 실행
        }
    </script>
</th:block>
```
- 25강 실패 커밋(025-2): ul 바로 아래 script는 폼이 아직 완성되지 않아 focus 실패 → `setTimeout`(1초 → 0.001초 → 생략)으로 해결.
- 프래그먼트는 `th:object` 내부(form 안)에 `th:replace`로 삽입해야 `#fields`가 동작.
- 24-3: `data-error-field-name="${...}"`처럼 `th:` 없이 쓰면 평가되지 않음 → `th:data-...`로 수정.

### 6-5. 출력/유틸리티 [확인]
- `th:text`(이스케이프, 기본) vs `th:utext`(비이스케이프, XSS 위험) 비교, 인라인 `[[...]]` / `[(...)]` (16강). 최종 코드는 `[[${post.title}]]` 인라인 사용.
- `#temporals.format(post.createDate, 'yy.MM.dd HH:mm')` 날짜 포맷.
- 리터럴 치환 `th:text="|${post.id} : ${post.title}|"`.
- 줄바꿈 표시는 `style="white-space: pre-line;"` (교재 [교재-기억]은 마크다운 변환 전까지 `th:text` 그대로).
- 링크 표현식: `th:href="@{/posts/{id}(id=${post.id})}"`, 상대경로 `@{{id}/modify(id=${post.id})}`, `href="."`, `href="write"`.
- `th:block`, `th:with`, `post.comments.reversed`(Java 21+ `List.reversed()`)로 최신 댓글 먼저.
- `<form method="POST" th:action>`: action 생략(현재 URL) + CSRF hidden 자동 삽입.

### 6-6. HTTP 메서드: HiddenHttpMethodFilter + a[method] (41~44강) [확인]
- 교재 [교재-기억]: 삭제는 `<a href="javascript:void(0);" th:data-uri="@{|/question/delete/${id}|}" class="delete">` + 하단 스크립트에서 confirm 후 `location.href`로 **GET** 요청, 컨트롤러는 `@GetMapping("/question/delete/{id}")`.
- 강사:
  - `spring.mvc.hiddenmethod.filter.enabled: true`
  - 수정 폼: `<input type="hidden" name="_method" value="PUT">` → `@PutMapping("/posts/{id}/modify")`
  - 삭제 링크: `<a method="DELETE" th:href="@{...}" onclick="return confirm('정말 삭제하시겠습니까?');">삭제</a>` → `@DeleteMapping("/posts/{postId}/comments/{id}/delete")`
  - `static/resource/global.js`가 `a[method]` 클릭 시 동적 POST form 생성(`_csrf`는 `<meta name="_csrf">`에서, `_method`, 쿼리스트링 → hidden input) 후 submit. `onclick`은 `data-onclick`으로 옮겨 confirm 결과가 false면 중단.
  - `select[value]` 속성으로 기본 option 선택하는 유틸도 같은 파일.
- 이유: GET으로 상태 변경하지 않기(멱등성, 7강), CSRF 보호를 받으려면 POST 계열이어야 함.
- 26강: URL은 `GET/POST /posts/write`처럼 같게 두고 메서드로 구분 (REST API라면 `POST /posts`).

### 6-7. 클라이언트 측 검증 (35, 39강) [확인]
```html
<form method="POST" th:action="@{/posts/{id}/comments/write(id=${post.id})}"
      onsubmit="PostComment__submitWriteForm(this); return false;">
<script>
function PostComment__submitWriteForm(form) {
    form.content.value = form.content.value.trim();
    if (form.content.value.length === 0) { alert("댓글 내용을 입력해주세요."); form.content.focus(); return; }
    if (form.content.value.length < 2) { alert("댓글 내용을 2자 이상 입력해주세요."); form.content.focus(); return; }
    form.submit();
}
</script>
```
- 함수 이름 규칙 `엔티티__동작` (예: `PostComment__submitModifyForm`).
- 서버 검증은 반드시(댓글 컨트롤러는 `@Valid WriteForm`만, BindingResult 없이 실패 시 예외), 클라이언트 검증은 UX용. `maxlength="100"` 속성 병행.

### 6-8. CSRF / Security (46강) [확인]
```java
// global/security/SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/favicon.ico").permitAll()
        .requestMatchers("/resource/**").permitAll()
        .requestMatchers("/h2-console/**").permitAll()
        .requestMatchers("/**").permitAll() // 추후 상황에 맡게 변경
        .anyRequest().authenticated())
    .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
    .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
```
- 정적 파일을 `static/resource/` 하위로 모아 `/resource/**` 한 줄로 허용.
- CSRF 처리 진화: 046-3 수동 `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}">` → 046-4 `th:action`으로 자동 → 046-5 JS 요청용 `<meta name="_csrf">`.
- 교재 [교재-기억]: 교재도 `th:action` 사용하지만 초기에는 `/h2-console` 때문에 `csrf().ignoringRequestMatchers` 및 frameOptions sameOrigin 설정 — 거의 동일.

### 6-9. 리다이렉트 (27~28강) [확인]
- meta refresh 방식을 거쳐 `return "redirect:/posts/" + post.getId();` (302)로 정리. 작성/수정 후 상세 페이지로.
- `GET /posts/` → `redirect:/posts` 처리 메서드 존재 (`redirectToList`).

### 6-10. CSS 프레임워크 [확인]
- 사용 안 함(순수 HTML, 인라인 스타일 `color: red`, `white-space: pre-line` 정도). Bootstrap/jQuery CDN은 layout.html에 주석으로만 남아 있음. Tailwind/daisyUI, toastr 등 없음.

---

## 7. 테스트 [확인]
- `src/test/java/com/back/BackApplicationTests.java` 하나뿐: `@ActiveProfiles("test") @SpringBootTest` + `contextLoads()`.
- MockMvc, 컨트롤러/서비스 테스트, assertion 스타일 **없음**. 테스트 의존성(`*-test` 스타터들)만 준비되어 있음.
- 테스트 프로파일은 H2 메모리 DB(`application-test.yaml`).

---

## 8. 커밋 / README 규칙

### 이 저장소 [확인]
- 커밋 메시지 = 강의 번호 태그만(`NNN` 또는 `NNN-k`), 본문 없음. 실패하는 중간 상태도 그대로 커밋(033-1, 037-1, 016-5, 016-7, 024-3, 025-2 등)해서 "왜 필요한지"를 보여줌.
- README.md 없음.

### 47강 미션에서 요구하는 규칙 (학습 프로젝트에 적용할 것) [확인]
- 참고 자료: 교재 https://wikidocs.net/book/7601, GitHub https://github.com/pahkey/sbb3, 14163(점프 투 스프링부트 개선) https://www.slog.gg/p/14163/raw / https://github.com/jhs512/p-14163-2, 14168(본 강의), 도전과제 https://wikidocs.net/162814
- 절대 규칙: AI는 이해 속도보다 앞서가지 않음, 전체 방향 먼저 설명, 한 번에 작은 작업 하나, 승인 전 수정/진행 금지, 코드 설명, 실행 결과를 데스크톱/브라우저 도구로 중간 확인, 테스트 실행, 작은 단위 커밋, **모든 커밋에서 README.md `## 작업내역` 갱신**.
- 진행 순서: 교재 기능 확인 → 관련 강의와 비교 → 개선점/작업 단위 결정 → 작은 작업 하나 구현 → 설명·실행 검증 → README 기록·커밋 → 승인 후 다음.
- 커밋 제목: `기능번호-세부번호 기능명, 세부 작업` (예: `078-04 회원가입 기능, 입력값 검증 추가`)
- 커밋 본문:
```text
078-04 회원가입 기능, 입력값 검증 추가
작업 내용:
- @Valid, BindingResult 적용
- 검증 오류 메시지 출력
작업 이유:
- 잘못된 회원 정보 저장 방지
- 14168 강의의 검증 방식 적용
검증:
- 정상 입력 및 오류 입력 테스트 통과
```
- README:
```markdown
## 작업내역
### 078-01 회원가입 기능, Spring Security 의존성 추가
- [링크](https://~~/tag-name)
- 작업: Security 의존성 추가
- 이유: 회원 인증 기능 구현 준비
- 결과: 애플리케이션 정상 실행
```
- 원칙: **커밋 하나 = 작은 작업 하나 + README 기록 하나**. 링크는 태그 이름 기반(강사 저장소처럼 커밋에 태그를 다는 방식으로 보임 [추론]).

---

## 9. SBB 재구현 시 적용 체크리스트 (요약)
1. `BaseEntity`(@MappedSuperclass, IDENTITY id, @CreatedDate/@LastModifiedDate, id 기반 equals/hashCode) + `@EnableJpaAuditing`
2. 엔티티 @Setter 금지, 생성자 + `modify()` 등 도메인 메서드
3. `Question`–`Answer`: `@OneToMany(mappedBy, cascade={PERSIST,REMOVE}, orphanRemoval=true)` + `@ManyToOne(fetch=LAZY)`, `question.addAnswer()/deleteAnswer()/findAnswerById()`로 애그리거트 경유 (AnswerRepository 생략 여부는 선택)
4. save 없이 더티체킹, `@Transactional`/`@Transactional(readOnly=true)`를 컨트롤러 액션에 부착(강사 방식) — 혹은 서비스로 옮길지 결정
5. `BaseInitData` (ApplicationRunner + `@Lazy self` + `@Transactional`, count>0이면 skip)
6. 프로파일 분리 yaml (dev 파일 H2 MODE=MySQL, test 메모리), SQL/바인딩/트랜잭션 TRACE 로그, `hikari.auto-commit=false`, dev에서 thymeleaf cache off + file prefix
7. 폼: 컨트롤러 내부 record, `@ModelAttribute("form")`, GET에도 form 파라미터(@Valid 없이), `th:object`/`th:field`
8. 에러: 메시지 `NN-field-msg` 규칙 + `form_errors.html` 프래그먼트(정렬 + 첫 에러 필드 focus)
9. layout dialect + `layout:title-pattern` + `global.js`(a[method], select[value]) + `<meta name="_csrf">`
10. HiddenHttpMethodFilter → `@PutMapping`/`@DeleteMapping`, 삭제는 `a[method="DELETE"]` + confirm
11. Security: `/resource/**`, h2-console 허용, frameOptions sameOrigin, `th:action`으로 CSRF
12. 이 강의에 **없는** 것(교재/14163에서 따로 확인 필요): 페이징, 검색(Specification/@Query), N+1 대책, 예외 처리(DataNotFound), 회원/권한, 테스트(MockMvc).
