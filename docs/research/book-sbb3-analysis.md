# 점프 투 스프링부트(SBB) 분석 보고서

출처
- 책 목차: https://wikidocs.net/book/7601 (curl/WebFetch는 Cloudflare 403. 브라우저로 목차와 각 페이지 본문을 읽음)
- 추가 기능(도전 과제): https://wikidocs.net/162814
- 소스: https://github.com/pahkey/sbb3 (clone해서 확인. 브랜치는 `main` 하나. 태그는 챕터별로 v1.02~v4.12. 각 커밋 메시지가 챕터 번호, 예: `2-04`)
- "되새김 문제" 페이지(226558/226560/226561/226562)와 "정답 및 풀이"(226563)는 위키독스에서 비공개("유료 E-book 또는 종이책에서 확인")라 내용 확인 불가

---

## 1. 기술 스택과 버전

| 항목 | 책 본문 | sbb3 저장소 (최종 HEAD) | 근거 |
|---|---|---|---|
| Spring Boot | "3.x 기준". 1-02에 "최신 안정화 버전은 3.1.3" | **3.2.1** (`org.springframework.boot` 3.2.1, `io.spring.dependency-management` 1.1.4) | 0-04 (160032), 1-02 (160048), `sbb/build.gradle` |
| Java | 0-04: "자바 17 기준". 1-02: 프로젝트 생성 시 "Java Version 20 선택" | `sourceCompatibility = '21'`였다가 4-05 커밋에서 **'19'**로 바뀜 (서버 배포용으로 추정, 확실하지 않음) | `git log -S` 결과 4-05 |
| 빌드 | Gradle - Groovy | Gradle Wrapper **8.5** | `gradle-wrapper.properties` |
| IDE | STS (부록 A-01에서 IntelliJ 안내) | – | 160048, 164891 |
| DB | H2 (파일 DB `jdbc:h2:~/local`). 4-12에서 PostgreSQL로 전환 | `runtimeOnly h2`, `runtimeOnly org.postgresql:postgresql` | `application.properties`, `application-prod.properties` |
| JPA | spring-boot-starter-data-jpa, `ddl-auto=update`, show_sql/format_sql=true | 같음 | 2-03 |
| Lombok | `compileOnly` + `annotationProcessor` | 같음 | 1-04 |
| Devtools | `developmentOnly spring-boot-devtools` | 같음 | 1-04 |
| Thymeleaf | starter-thymeleaf + `nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect` (2-15) | 같음 | build.gradle |
| Validation | spring-boot-starter-validation (2-16) | 같음 | |
| Security | spring-boot-starter-security + `thymeleaf-extras-springsecurity6` (3-05) | 같음 | |
| Markdown | `org.commonmark:commonmark:0.21.0` (3-12) | 같음 | |
| Bootstrap | 0-04: "5.2.3 기준". 2-14: "bootstrap-5.3.1-dist.zip 다운로드" | static의 `bootstrap.min.css`/`.js`는 **v5.3.2** | 파일 헤더 |
| Test | spring-boot-starter-test, `junit-jupiter`, `junit-platform-launcher` | 같음 | |

`application.properties` (개발용):
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:~/local
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.show_sql=true
```
`application-prod.properties` (4-07에서 생성 → 4-09 로그 설정 추가 → 4-12에서 PostgreSQL로 교체): PostgreSQL 접속(`<DATABASE_ENDPOINT>`, `<DATABASE_PASSWORD>` 자리표시자), logback rolling 설정(`logs/sbb.log`, 100MB, 30일), `Asia/Seoul` 날짜 형식.

기타: group `com.mysite`, 패키지 `com.mysite.sbb`, version은 `0.0.1-SNAPSHOT`에서 4-07에 `0.0.2`로 변경.

> 버전 표기가 일관되지 않음: 책 본문은 Java 17/20, SB 3.1.3, Bootstrap 5.2.3/5.3.1. 저장소는 SB 3.2.1, Java 21→19, Bootstrap 5.3.2. 새로 구현한다면 최신 SB 3.x + Java 17/21로 진행해도 문제없음 (`AntPathRequestMatcher` 같은 deprecated API만 주의).

---

## 2. 전체 목차 (순서대로)

표기: **[이론]** 이론 위주 / **[구현]** 코드 변경 있음(저장소 태그) / **[환경]** 도구·인프라 작업

### 0장 들어가기 전에 — https://wikidocs.net/160022
| 절 | 제목 | URL | 유형 / 내용 |
|---|---|---|---|
| 0-01 | 머리말 | https://wikidocs.net/160028 | [이론] |
| 0-02 | 저자소개 | https://wikidocs.net/160029 | [이론] |
| 0-03 | 주요변경이력 | https://wikidocs.net/160031 | [이론] 2022.03 위키독스 출간 → 2022.12 SB 3.0.0 적용 → 2023.12 종이책 |
| 0-04 | 이 책을 읽기 전에 | https://wikidocs.net/160032 | [이론] 사전지식, 버전, 챕터별 소스 태그 안내 |
| 0-05 | 책 구입 안내 | https://wikidocs.net/226559 | – |

### 1장 스프링부트 개발 준비하기 — https://wikidocs.net/160023
| 절 | 제목 | URL | 유형 / 내용 |
|---|---|---|---|
| 1-01 | 스프링 부트란? | https://wikidocs.net/160047 | [이론] |
| 1-02 | 스프링 부트 개발 환경 준비하기 | https://wikidocs.net/160048 | [환경] JDK, STS 설치. Spring Starter Project 생성(sbb, Gradle-Groovy, Spring Web) |
| 1-03 | 스프링부트 맛보기 | https://wikidocs.net/160375 | [구현 v1.03] 클라이언트/서버 개념. `HelloController` `/hello` → `@ResponseBody` 문자열 |
| 1-04 | 스프링부트 도구 설치하기 | https://wikidocs.net/160444 | [구현 v1.04] Devtools, Lombok. `HelloLombok` (@Getter/@Setter → @RequiredArgsConstructor 실습) |
| – | 1장 되새김 문제 | https://wikidocs.net/226558 | 비공개 |

### 2장 스프링 부트의 기본 기능 익히기 — https://wikidocs.net/160024
| 절 | 제목 | URL | 유형 / 내용 |
|---|---|---|---|
| 2-01 | 스프링 부트 프로젝트의 구조 이해하기 | https://wikidocs.net/160947 | [이론] src/main/java, resources(templates/static/application.properties), test, build.gradle |
| 2-02 | 간단한 웹 프로그램 만들기 | https://wikidocs.net/160543 | [구현 v2.02] `MainController` `/sbb` @ResponseBody |
| 2-03 | JPA로 데이터베이스 사용하기 | https://wikidocs.net/161164 | [구현 v2.03] ORM/JPA 이론 + H2, JPA 의존성, application.properties, H2 콘솔 |
| 2-04 | 엔티티로 테이블 매핑하기 | https://wikidocs.net/161165 | [구현 v2.04] `Question`(id, subject, content, createDate, answerList @OneToMany cascade REMOVE), `Answer`(id, content, createDate, question @ManyToOne). "엔티티에는 Setter를 쓰지 않는 것이 좋지만 이 책은 편의상 사용" 박스 |
| 2-05 | 리포지터리로 데이터베이스 관리하기 | https://wikidocs.net/160890 | [구현 v2.05] `QuestionRepository`(findBySubject, findBySubjectAndContent, findBySubjectLike), `AnswerRepository`. JUnit으로 CRUD 실습(SbbApplicationTests). LazyInitialization 문제 → 테스트에 @Transactional |
| 2-06 | 도메인별로 분류하기 | https://wikidocs.net/161187 | [구현 v2.06] question/answer(/user) 패키지로 이동 |
| 2-07 | 질문 목록 만들기 | https://wikidocs.net/161186 | [구현 v2.07] `QuestionController` `/question/list`, Thymeleaf 의존성, `question_list.html`. Thymeleaf th:if/th:each/th:text. @RequiredArgsConstructor 생성자 주입 |
| 2-08 | 루트 URL 사용하기 | https://wikidocs.net/161354 | [구현 v2.08] `/` → `redirect:/question/list` |
| 2-09 | 서비스 활용하기 | https://wikidocs.net/161220 | [구현 v2.09] `QuestionService.getList()`. 서비스가 필요한 이유(모듈화, Entity→DTO 변환)를 설명하지만 **실제 DTO는 만들지 않음** |
| 2-10 | 상세 페이지 만들기 | https://wikidocs.net/161302 | [구현 v2.10] `/question/detail/{id}`, `getQuestion`, `DataNotFoundException`, `question_detail.html`, 목록에 링크 |
| 2-11 | URL 프리픽스 알아 두기 | https://wikidocs.net/161357 | [구현 v2.11] 클래스에 `@RequestMapping("/question")` |
| 2-12 | 답변 기능 만들기 | https://wikidocs.net/161396 | [구현 v2.12] `AnswerController` POST `/answer/create/{id}`, `AnswerService.create`, 상세 페이지에 textarea와 답변 목록 |
| 2-13 | 웹 페이지 디자인하기 | https://wikidocs.net/161459 | [구현 v2.13] static/`style.css`, 템플릿에 link |
| 2-14 | 부트스트랩으로 화면 꾸미기 | https://wikidocs.net/161523 | [구현 v2.14] bootstrap.min.css 복사, 목록/상세에 Bootstrap 클래스 적용 |
| 2-15 | 표준 HTML 구조로 변경하기 | https://wikidocs.net/161873 | [구현 v2.15] `layout.html` + `layout:decorate`/`layout:fragment` 템플릿 상속 |
| 2-16 | 질문 등록 기능 추가하기 | https://wikidocs.net/161911 | [구현 v2.16] `/question/create` GET/POST, `question_form.html`, Validation, `QuestionForm`(@NotEmpty, @Size(max=200)), `AnswerForm`, `form_errors.html` 공통 fragment |
| – | 2장 되새김 문제 | https://wikidocs.net/226560 | 비공개 |

### 3장 SBB 서비스 개발하기 — https://wikidocs.net/160025
| 절 | 제목 | URL | 유형 / 내용 |
|---|---|---|---|
| 3-01 | 내비게이션 바 추가하기 | https://wikidocs.net/161986 | [구현 v3.01] `navbar.html` fragment(`th:replace`), bootstrap.min.js(햄버거 메뉴) |
| 3-02 | 페이징 기능 추가하기 | https://wikidocs.net/162028 | [구현 v3.02] 테스트로 300건 생성, `Page<Question> findAll(Pageable)`, `getList(int page)` (10건, createDate desc), 페이지 이동(현재 ±5) |
| 3-03 | 게시물에 번호 지정하기 | https://wikidocs.net/162090 | [구현 v3.03] 번호 = `totalElements - number*size - loop.index` |
| 3-04 | 답변 개수 표시하기 | https://wikidocs.net/162092 | [구현 v3.04] `#lists.size(question.answerList)` 배지 |
| 3-05 | 스프링 시큐리티란? | https://wikidocs.net/162150 | [구현 v3.05] 의존성, `SecurityConfig`(permitAll `/**`), H2 콘솔 CSRF 예외 + X-Frame-Options SAMEORIGIN |
| 3-06 | 회원 가입 기능 구현하기 | https://wikidocs.net/162141 | [구현 v3.06] `SiteUser`(username/email unique), `UserRepository`, `UserService`(BCrypt), `UserCreateForm`(password1/2, @Email), `UserController` `/user/signup`, `signup_form.html`, 중복 가입 시 DataIntegrityViolationException 처리 |
| 3-07 | 로그인과 로그아웃 기능 구현하기 | https://wikidocs.net/162255 | [구현 v3.07] formLogin `/user/login`, `login_form.html`, `UserRole` enum, `UserSecurityService implements UserDetailsService`, AuthenticationManager bean, 로그아웃 `/user/logout`, navbar의 `sec:authorize` |
| 3-08 | 글쓴이 항목 추가하기 | https://wikidocs.net/162330 | [구현 v3.08] Question/Answer에 `@ManyToOne SiteUser author`, `Principal`로 작성자 저장, `@EnableMethodSecurity` + `@PreAuthorize("isAuthenticated()")`, 비로그인 시 textarea disabled, 목록·상세에 글쓴이 표시 |
| 3-09 | 수정과 삭제 기능 추가하기 | https://wikidocs.net/162413 | [구현 v3.09] modifyDate, 질문/답변 수정(GET 폼 채우기, POST), 삭제(GET `/delete/{id}` + JS confirm), 작성자 검사 `ResponseStatusException(BAD_REQUEST)`, `answer_form.html`, form `th:action` 생략 방식, layout에 `script` fragment |
| 3-10 | 추천 기능 추가하기 | https://wikidocs.net/162416 | [구현 v3.10] `@ManyToMany Set<SiteUser> voter`, `/question/vote/{id}`, `/answer/vote/{id}`, 추천 수 배지 |
| 3-11 | 앵커 기능 추가하기 | https://wikidocs.net/162657 | [구현 v3.11] `<a th:id="answer_{id}">`, 리다이렉트 `#answer_%s`, `AnswerService.create`가 Answer 반환 |
| 3-12 | 마크다운 적용하기 | https://wikidocs.net/162763 | [구현 v3.12] commonmark, `CommonUtil` @Component, `th:utext="${@commonUtil.markdown(...)}"` |
| 3-13 | 검색 기능 추가하기 | https://wikidocs.net/162799 | [구현 v3.13] JPA Specification(조인 검색) → 이후 `@Query` JPQL `findAllByKeyword`로 교체. 검색창, hidden `searchForm`(kw, page), 페이징 링크를 `data-page` + JS로 변경 |
| 3-14 | SBB 추가 기능 구현하기 | https://wikidocs.net/162814 | [도전 과제] 아래 6절 |
| – | 3장 되새김 문제 | https://wikidocs.net/226561 | 비공개 |

### 4장 세상에 선보이는 SBB 서비스! — https://wikidocs.net/160026
| 절 | 제목 | URL | 유형 / 내용 |
|---|---|---|---|
| 4-01 | 이제 서버가 필요하다! | https://wikidocs.net/162888 | [이론] |
| 4-02 | AWS 라이트세일 알아보기 | https://wikidocs.net/162889 | [환경] 가입, 인스턴스 생성 |
| 4-03 | 서버 접속 설정하기 | https://wikidocs.net/162905 | [환경] 고정 IP, 방화벽 |
| 4-04 | 서버 접속 프로그램 설치하기 | https://wikidocs.net/162895 | [환경] 키, SSH, SFTP |
| 4-05 | SBB 배포하기 | https://wikidocs.net/162890 | [환경 + v4.05] 서버 자바 설치, jar 빌드, SFTP 전송 (build.gradle의 Java 버전 변경) |
| 4-06 | 서버 스크립트 생성하기 | https://wikidocs.net/162981 | [환경] start.sh/stop.sh (저장소에는 없음) |
| 4-07 | 서버 환경으로 분리하기 | https://wikidocs.net/163021 | [구현 v4.07] `application-prod.properties`, 버전 0.0.2, 서버 H2 콘솔 |
| 4-08 | 80번 포트로 웹 서비스 운영하기 | https://wikidocs.net/163117 | [환경] Nginx 리버스 프록시 |
| 4-09 | 로그 관리하기 | https://wikidocs.net/163118 | [구현 v4.09] 로그 파일 분리, 컨트롤러에 `@Slf4j` + `log.info` |
| 4-10 | 도메인 사용하기 | https://wikidocs.net/163137 | [환경] |
| 4-11 | HTTPS로 전환하기 | https://wikidocs.net/163144 | [환경] 인증서 발급(Let's Encrypt로 추정, 확인 안 함) |
| 4-12 | PostgreSQL로 전환하기 | https://wikidocs.net/163143 | [구현 v4.12] postgresql 드라이버, prod 설정 교체 |
| – | 4장 되새김 문제 | https://wikidocs.net/226562 | 비공개 |

### 부록 등
| 절 | 제목 | URL | 내용 |
|---|---|---|---|
| A | 부록 | https://wikidocs.net/163574 | |
| A-01 | 인텔리제이 사용하기 | https://wikidocs.net/164891 | Initializr, Lombok 플러그인, Auto Reload, Gradle 실행/빌드 |
| A-02 | AWS 라이트세일 사용 취소 | https://wikidocs.net/163575 | |
| A-03 | 스프링부트 2.x 버전 안내 | https://wikidocs.net/182149 | jakarta→javax, 2.x용 SecurityConfig |
| A-04 | 댓글 (삭제예정) | https://wikidocs.net/162570 | **구버전 기준** 댓글 구현(Comment 엔티티/Repository/Service/Form/Controller/comment_form.html). 도전 과제 "댓글"의 참고 자료 |
| B | 마치며 | https://wikidocs.net/160027 | |
| C | 정답 및 풀이 | https://wikidocs.net/226563 | 비공개 |
| – | 도서 증정 이벤트 | https://wikidocs.net/456154 | |

---

## 3. 구현 기능 단위 (커밋 단위 제안, 순서대로)

괄호 안은 원본 태그.

1. 프로젝트 생성: Spring Web, Gradle, `SbbApplication`, 기본 테스트 (시작 커밋)
2. Hello 컨트롤러 (v1.03). 생략 가능
3. Devtools + Lombok 추가 (v1.04)
4. `MainController` `/sbb` (v2.02)
5. JPA + H2 설정 (v2.03)
6. 엔티티 Question/Answer (v2.04)
7. 리포지터리 + JPA 학습 테스트 (v2.05)
8. 도메인별 패키지 분리 (v2.06)
9. 질문 목록 + Thymeleaf (v2.07)
10. 루트 URL 리다이렉트 (v2.08)
11. QuestionService 도입 (v2.09)
12. 질문 상세 + DataNotFoundException (v2.10)
13. URL 프리픽스 `@RequestMapping` (v2.11)
14. 답변 등록 (v2.12)
15. 스타일시트 (v2.13)
16. 부트스트랩 적용 (v2.14)
17. 템플릿 상속 layout.html (v2.15)
18. 질문 등록 + 폼 검증(QuestionForm/AnswerForm/form_errors) (v2.16)
19. 내비게이션 바 (v3.01)
20. 페이징 (+300건 테스트 데이터) (v3.02)
21. 게시물 번호 (v3.03)
22. 답변 개수 (v3.04)
23. Spring Security 도입 + H2 콘솔 예외 (v3.05)
24. 회원가입 (v3.06)
25. 로그인/로그아웃 (v3.07)
26. 엔티티에 author 추가 + 글쓴이 저장/표시 + @PreAuthorize (v3.08)
27. 질문/답변 수정·삭제 + modifyDate (v3.09). 질문 수정 / 질문 삭제 / 답변 수정 / 답변 삭제 / 수정일시 표시로 더 나눌 수 있음
28. 추천(voter) (v3.10). 질문 추천과 답변 추천으로 나눌 수 있음
29. 앵커 (v3.11)
30. 마크다운 (v3.12)
31. 검색: Specification 버전 → @Query 버전 (v3.13)
32. (배포) prod 프로파일 분리 (v4.07), 로그 설정 + @Slf4j (v4.09), PostgreSQL (v4.12). 4-01~4-06, 4-08, 4-10, 4-11은 인프라 작업이라 코드 커밋 없음

---

## 4. sbb3 최종 구조와 코드 패턴

### 패키지 구조 (`sbb/src/main/java/com/mysite/sbb/`)
```
SbbApplication.java
HelloController.java, HelloLombok.java      # 1장 실습 잔재 (최종본에도 남아 있음)
MainController.java                         # /sbb, / → redirect
CommonUtil.java                             # @Component markdown()
DataNotFoundException.java
SecurityConfig.java
question/  Question, QuestionController, QuestionForm, QuestionRepository, QuestionService
answer/    Answer, AnswerController, AnswerForm, AnswerRepository, AnswerService
user/      SiteUser, UserController, UserCreateForm, UserRepository, UserRole, UserSecurityService, UserService
resources/templates: layout, navbar, form_errors, question_list, question_detail, question_form, answer_form, signup_form, login_form
resources/static: bootstrap.min.css, bootstrap.min.js, style.css
test: SbbApplicationTests.java (하나만 있음)
```
DTO 계층과 별도 config/exception 패키지는 없음. 레이어는 엔티티 + 컨트롤러 + 서비스 + 리포지터리 + 폼.

### 개선 대상이 될 만한 패턴 (구체적으로)

**(1) 의존성 주입**: 본 코드는 모두 `@RequiredArgsConstructor` + `private final` 생성자 주입이고, `@Autowired` 필드 주입은 **테스트 코드에서만** 사용함 (`git log -S'@Autowired' -- sbb/src/main` 결과 없음). 2-05에 "JUnit이 생성자 주입을 지원하지 않으므로 테스트만 @Autowired"라는 설명이 있는데, JUnit 5 + `@SpringBootTest`는 생성자 주입(`@Autowired` 생성자 / `@TestConstructor`)을 지원하므로 부정확한 설명임.

**(2) 엔티티 @Getter @Setter + 서비스에서 setter로 조립**: 책도 "원래는 Setter를 쓰지 않는 게 좋다"고 인정함(2-04).
```java
@Getter @Setter @Entity
public class Question {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(length = 200) private String subject;
    @Column(columnDefinition = "TEXT") private String content;
    private LocalDateTime createDate;
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE) private List<Answer> answerList;
    @ManyToOne private SiteUser author;
    private LocalDateTime modifyDate;
    @ManyToMany Set<SiteUser> voter;   // private 없음 (package-private)
}
// QuestionService.create
Question q = new Question();
q.setSubject(subject); q.setContent(content);
q.setCreateDate(LocalDateTime.now()); q.setAuthor(user);
this.questionRepository.save(q);
```
- 생성자/정적 팩토리/빌더 없음. 기본 생성자 접근 제한(`@NoArgsConstructor(access=PROTECTED)`) 없음
- `createDate`/`modifyDate`를 `LocalDateTime.now()`로 직접 넣음. JPA Auditing(`@CreatedDate`, `@LastModifiedDate`)이나 BaseTimeEntity 없음
- `@ManyToOne`에 fetch 지정 없음 (기본 EAGER)
- `voter`에 `private` 없음, 초기화 안 함 (`new HashSet<>()` 없음)
- 양방향 연관관계 편의 메서드 없음. `answerList` 초기화 안 함
- `SiteUser.id`는 Long, Question/Answer id는 Integer로 타입이 섞여 있음

**(3) 서비스**: `@Transactional`이 **어디에도 없음**(클래스/메서드 모두). 수정도 dirty checking 대신 `save()`를 다시 호출함.
```java
public void modify(Question question, String subject, String content) {
    question.setSubject(subject); question.setContent(content);
    question.setModifyDate(LocalDateTime.now());
    this.questionRepository.save(question);
}
public Question getQuestion(Integer id) {
    Optional<Question> question = this.questionRepository.findById(id);
    if (question.isPresent()) return question.get();
    else throw new DataNotFoundException("question not found");   // orElseThrow 미사용
}
```
- 서비스가 엔티티를 그대로 반환하고, 템플릿에서 지연 로딩(answerList, voter, author)에 접근함 (OSIV 기본값 true에 의존)
- `getList`: `PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate")))`. 페이지 크기가 하드코딩됨. 음수 page 검증 없음

**(4) 예외 처리**
```java
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "entity not found")
public class DataNotFoundException extends RuntimeException { ... }
```
- 권한 검사는 컨트롤러에서 `throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.")`. 403이 아니라 400이고, 같은 코드가 6번 반복됨 (질문/답변 × 수정 GET/POST, 삭제)
- `@ControllerAdvice`나 에러 페이지 없음
- 회원가입에서 `catch (Exception e) { e.printStackTrace(); bindingResult.reject("signupFailed", e.getMessage()); }`

**(5) Form 클래스**: `@Getter @Setter` + Bean Validation. 컨트롤러에서 GET 시 빈 폼을 파라미터로 받아 템플릿에 노출하는 패턴(`public String questionCreate(QuestionForm questionForm)`)과, 수정 화면도 같은 `question_form.html`을 재사용하는 패턴(`th:action` 생략 → 현재 URL로 POST)을 씀.
```java
public class UserCreateForm {
    @Size(min = 3, max = 25) @NotEmpty(message = "사용자ID는 필수항목입니다.") private String username;
    @NotEmpty private String password1; @NotEmpty private String password2;
    @NotEmpty @Email private String email;
}
```
비밀번호 일치 검사는 컨트롤러에서 `bindingResult.rejectValue("password2", "passwordInCorrect", ...)`로 함. 중복 검사는 DB 제약 위반 예외에 의존함.

**(6) 컨트롤러**: `Principal principal` → `userService.getUser(principal.getName())`. 삭제와 추천을 **GET**으로 처리함(`@GetMapping("/delete/{id}")`, `@GetMapping("/vote/{id}")`. JS `location.href`로 호출). CSRF 보호를 받지 않음. 추천은 같은 사용자의 중복 추천을 Set으로만 막고, 추천 취소는 없음.
```java
return String.format("redirect:/question/detail/%s#answer_%s", answer.getQuestion().getId(), answer.getId());
```
`AnswerController.createAnswer`는 검증 실패 시 `question_detail`을 직접 렌더링함.

**(7) Security**
```java
http.authorizeHttpRequests(a -> a.requestMatchers(new AntPathRequestMatcher("/**")).permitAll())
    .csrf(c -> c.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**")))
    .headers(h -> h.addHeaderWriter(new XFrameOptionsHeaderWriter(SAMEORIGIN)))
    .formLogin(f -> f.loginPage("/user/login").defaultSuccessUrl("/"))
    .logout(l -> l.logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")).logoutSuccessUrl("/").invalidateHttpSession(true));
```
- URL은 모두 permitAll이고, 보호는 `@EnableMethodSecurity(prePostEnabled = true)` + `@PreAuthorize`에만 의존함
- `AntPathRequestMatcher`는 Spring Security 6.x 후속 버전에서 deprecated 예정. GET 로그아웃
- `UserSecurityService`: `if ("admin".equals(username))`이면 ADMIN 권한을 줌 (하드코딩). 리포지터리 메서드 이름이 `findByusername` (소문자 u)
- `UserRole` enum은 DB에 저장하지 않음
- navbar의 회원가입 링크는 로그인한 상태에서도 보임
- 템플릿의 수정/삭제 버튼은 `sec:authorize="isAuthenticated()"` + `th:if="${...author != null and #authentication.getPrincipal().getUsername() == ...author.username}"` 방식 (question_detail.html에서 확인함)

**(8) 리포지터리 / 검색**
```java
Page<Question> findAll(Pageable pageable);                      // JpaRepository에 이미 있는 것과 중복 선언
Page<Question> findAll(Specification<Question> spec, Pageable pageable); // JpaSpecificationExecutor 없이 선언
@Query("select distinct q from Question q left outer join SiteUser u1 on q.author=u1 "
     + "left outer join Answer a on a.question=q left outer join SiteUser u2 on a.author=u2 "
     + "where q.subject like %:kw% or q.content like %:kw% or u1.username like %:kw% "
     + "or a.content like %:kw% or u2.username like %:kw% ")
Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
```
`QuestionService.search()` (Specification)는 `@SuppressWarnings("unused")`로 남아 있는 사용하지 않는 코드임. 목록에서 `#lists.size(question.answerList)`를 쓰므로 N+1 쿼리가 생김.

**(9) 템플릿/뷰**
- 마크다운은 `th:utext="${@commonUtil.markdown(question.content)}"`로 렌더링하는데 **HTML sanitize가 없어 XSS 위험**이 있음 (commonmark 기본 설정은 raw HTML을 통과시킴. `escapeHtml(true)` 미사용)
- 페이징/검색은 hidden form + 바닐라 JS로 구현. 페이지 번호가 0부터 표시됨
- `CommonUtil`은 Spring 빈으로 템플릿에서 직접 호출함

**(10) 기타**: `@Slf4j`는 QuestionController에만 있음 (4-09). 1장 실습 클래스(HelloController, HelloLombok)가 최종본에 남아 있음. 개발 DB가 파일 기반(`~/local`)이라 테스트와 개발 데이터가 섞임.

---

## 5. 책의 테스트 방식

- `src/test/java/com/mysite/sbb/SbbApplicationTests.java` **하나만** 사용함. `@SpringBootTest` + `@Autowired` 필드 주입. 실제 H2 파일 DB(`~/local`)에 그대로 붙어서 실행하므로 롤백이나 격리가 없음
- 2-05 (https://wikidocs.net/160890): 학습용 JPA 테스트를 **같은 testJpa() 메서드 내용을 계속 바꿔 가며** 진행함. 저장(q1, q2 save) → findAll(`assertEquals(2, all.size())`) → findById → findBySubject → findBySubjectAndContent → findBySubjectLike → 수정 → 삭제(`count()` 확인) → 답변 저장 → 답변 조회 → `q.getAnswerList()`에서 LazyInitializationException이 나서 `@Transactional` 추가. 한 단계가 이전 단계에서 들어간 데이터에 의존하고(id 1, 2), 다시 실행하면 깨지는 구조임. v2.05 태그의 최종본:
```java
@Transactional @Test
void testJpa() {
    Optional<Question> oq = this.questionRepository.findById(2);
    assertTrue(oq.isPresent());
    List<Answer> answerList = oq.get().getAnswerList();
    assertEquals(1, answerList.size());
    assertEquals("네 자동으로 생성됩니다.", answerList.get(0).getContent());
}
```
- 3-02 (https://wikidocs.net/162028): 같은 테스트를 **테스트 데이터 생성기**로 바꿈 (assert 없음). 3-08에서 시그니처가 바뀌어 `create(subject, content, null)`로 수정함:
```java
for (int i = 1; i <= 300; i++) {
    String subject = String.format("테스트 데이터입니다:[%03d]", i);
    this.questionService.create(subject, "내용무", null);
}
```
- 컨트롤러/MockMvc 테스트, 시큐리티 테스트, 슬라이스 테스트(@DataJpaTest), 단위 테스트는 **없음**. 최종 저장소의 유일한 테스트가 실행할 때마다 DB에 300건을 넣는 코드라는 점은 개선 포인트로 명확함.

---

## 6. 도전 과제 (3-14, https://wikidocs.net/162814)

책 원문 요약 → 하위 작업 제안 (하위 작업은 내 제안이며 책에 있는 내용이 아님)

### 6-1. 답변 페이징과 정렬
원문: 답변이 많아질 수 있으니 답변 페이징이 필요하다. 최신순, 추천순 정렬도 필요하다 (StackOverflow/Reddit처럼 추천 많은 답변을 먼저). 질문 목록 페이징 경험을 응용하면 된다.
- AnswerRepository에 `Page<Answer> findByQuestion(Question, Pageable)` 추가
- 상세 컨트롤러에 `answerPage`, `sort`(latest|vote) 파라미터 추가
- 추천순 정렬: `voter` 크기 기준 JPQL(`order by size(a.voter) desc`) 또는 voteCount 컬럼
- 상세 템플릿에 답변 페이징 UI와 정렬 토글. 앵커 리다이렉트(`#answer_id`) 시 해당 답변이 있는 페이지 계산
- 답변 개수 표시를 `answerList.size` → count 쿼리로 바꿀지 검토

### 6-2. 댓글
원문: Comment 엔티티를 만들고 Question, Answer와 연결한다. (참고: 부록 A-04 https://wikidocs.net/162570 의 구버전 구현. Comment{id, author, content, createDate, modifyDate, @ManyToOne question, @ManyToOne answer})
- Comment 엔티티 + Repository
- 질문 댓글 작성 (CommentForm, POST, 로그인 필요)
- 답변 댓글 작성
- 상세 화면에 댓글 목록 표시
- 댓글 수정/삭제 (작성자 검사)
- 앵커 `#comment_id`

### 6-3. 카테고리
원문: '질문답변' 외에 '강좌', '자유게시판' 등을 추가한다. Category 엔티티를 만들고 Question에 연결한다.
- Category 엔티티 + 초기 데이터(data.sql 또는 CommandLineRunner)
- Question에 `@ManyToOne Category`
- 목록 URL에 카테고리 추가(`/question/list/{category}`) + 리포지터리 쿼리에 카테고리 조건
- 질문 등록 폼에 카테고리 선택
- 네비게이션 바에 카테고리 메뉴
- 검색과 페이징에 카테고리 유지

### 6-4. 비밀번호 찾기와 변경
원문: 비밀번호를 분실하면 가입 이메일로 임시 비밀번호를 보내 로그인하게 한다. 로그인 후 기존 비밀번호와 새 비밀번호를 받아 변경한다.
- 비밀번호 변경: 폼(현재/새/확인), `passwordEncoder.matches` 검증, 변경 후 처리
- spring-boot-starter-mail 설정(로컬은 MailHog/GreenMail 등)
- 비밀번호 찾기: 이메일 입력 → 임시 비밀번호 생성/저장(BCrypt) → 메일 발송
- (선택) 임시 비밀번호 로그인 시 변경 유도

### 6-5. 프로필 화면
원문: 로그인한 사용자의 기본 정보와 작성한 질문, 답변, 댓글을 확인하는 화면.
- `/user/profile` (로그인 필요)
- 기본 정보 표시
- 내가 쓴 질문/답변/댓글 목록 (리포지터리 `findByAuthor` + 페이징)
- 탭 UI

### 6-6. 최근 답변 / 최근 댓글 노출
원문: 질문 위주 목록 외에 최근 작성된 답변과 댓글을 확인하는 기능.
- `findTop10ByOrderByCreateDateDesc` (Answer, Comment)
- 사이드바나 별도 페이지로 표시하고 해당 질문 앵커로 링크

### 6-7. 조회 수
원문: 답변 수는 표시하지만 조회 수는 없으니 추가한다.
- Question에 `viewCount` (int, 기본값 0)
- 상세 조회 시 증가 (`@Modifying` update 쿼리 또는 트랜잭션 내 증가)
- 목록에 컬럼 추가
- (선택) 새로고침 중복 증가 방지 (쿠키/세션)

### 6-8. 소셜 로그인
원문: 구글, 페이스북, 트위터 등을 경유한 로그인.
- spring-boot-starter-oauth2-client + provider 설정 (client-id/secret은 환경변수로)
- `OAuth2UserService` 구현: 이메일 기준으로 SiteUser 찾기 또는 가입
- SecurityConfig에 `oauth2Login` 추가, 로그인 화면에 버튼
- 기존 `principal.getName()` 의존 코드가 OAuth2 사용자에서도 동작하도록 정리 (username 매핑)

### 6-9. 마크다운 에디터
원문: 마크다운 에디터를 적용한다. 필자는 simpleMDE(simplemde.com)를 추천한다.
- question_form / answer_form의 textarea에 SimpleMDE 적용 (layout의 `script` fragment 사용)
- 상세 화면 답변 입력란에도 적용
- 참고: SimpleMDE는 유지보수가 중단되어 후속 포크 EasyMDE를 고려할 만함 (일반 지식이며 책에 없는 내용)

---

## 불확실한 점
- 되새김 문제와 정답(226558/226560/226561/226562/226563)은 비공개라 확인 못 함
- 4-05에서 Java 21→19로 바꾼 이유는 페이지 본문을 자세히 보지 않았음 (서버 JDK 버전에 맞춘 것으로 추정)
- 4-11 HTTPS 인증서 발급 도구는 원문을 직접 확인하지 않음 (수정/삭제 버튼의 `th:if` 조건과 question_form/answer_form에 `th:action`이 없는 점은 저장소에서 확인함)
- 책 본문 코드와 저장소 태그가 세부적으로 다를 수 있음 (저장소는 2024-01-10 기준 종이책 3판 소스)
