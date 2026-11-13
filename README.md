# jsb-plus

『점프 투 스프링부트』의 SBB(질문·답변 게시판)를 구현하면서, 장희성 강사의 수업 내용으로 개선한 프로젝트입니다.

- **최상위 목표**: 완성된 소스코드를 내가 이해하는 것
- **진행 방식**: AI 코딩 에이전트와 함께 "설명 → 승인 → 구현 → 검증 → 기록 → 커밋"을 작은 단위로 반복
- 상세 계획과 진행 규칙: [PLAN.md](PLAN.md)
- 설계 결정 기록: [docs/decisions.md](docs/decisions.md)
- 책 대비 개선점 모음: [docs/improvements.md](docs/improvements.md)

## 참고 자료

| 자료 | 링크 |
|---|---|
| 점프 투 스프링부트 (교재) | [위키독스](https://wikidocs.net/book/7601) / [GitHub sbb3](https://github.com/pahkey/sbb3) |
| 14163 점프 투 스프링부트 개선 | [강의](https://www.slog.gg/p/14163/raw) / [GitHub](https://github.com/jhs512/p-14163-2) |
| 14168 JPA, 타임리프 개선 | [강의](https://www.slog.gg/p/14168/raw) / [GitHub](https://github.com/jhs512/p-14168-2) |
| 추가 기능 도전과제 | [위키독스 3-14](https://wikidocs.net/162814) |

## 기술 스택

- Java 25, Spring Boot 4, Gradle (Kotlin DSL)

## 작업내역

### 000-01 프로젝트 준비, 완료, 저장소와 기록 문서 구성
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/000-01)
- 작업: git 저장소 초기화, .gitignore, README, PLAN.md(진행 규칙·로드맵), docs(설계 결정·개선점·분석 자료) 구성
- 이유: 모든 작업을 작은 커밋으로 기록하고, 설계 결정과 개선점을 쌓아 이해와 영상 제작의 재료로 쓰기 위함
- 결과: 첫 커밋 생성, GitHub 공개 저장소에 업로드

### 001-01 프로젝트 생성, 완료, Spring Boot 4 프로젝트 생성 및 실행 확인
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/001-01)
- 교재: 1-02 / 강의: 14163 2강
- 작업: Spring Initializr로 프로젝트 생성 (Boot 4.1.2, Java 25, Gradle Kotlin DSL, Web·Lombok·DevTools), 설정 파일을 yaml로 변경, 학습용 주석 추가
- 이유: 앱의 뼈대를 강사와 같은 최신 환경으로 준비
- 결과: 기본 테스트(contextLoads) 통과, 서버 실행 후 localhost:8080에서 404 페이지 확인(아직 페이지 없음)

### 000-02 프로젝트 준비, 진행 방식을 기능 단위 승인으로 변경
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/000-02)
- 작업: PLAN.md 4장 "진행 순서"를 기능 단위 승인 + IntelliJ에서 코드 보기로 변경
- 이유: 채팅창보다 IntelliJ에서 직접 코드를 보는 것이 이해에 유리하고, 기능 단위로 끊어도 이해 속도를 넘지 않음
- 결과: 이후 기능부터 새 방식 적용

### 002-01 첫 컨트롤러, 완료, MainController와 @ResponseBody
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/002-01)
- 교재: 2-02 / 강의: 14163 3~4강
- 작업: MainController에 `/`, `/hello` 추가, `@ResponseBody`로 문자열 응답
- 이유: 주소와 메서드를 연결하는 방법(@GetMapping)과 요청 흐름(DispatcherServlet → 컨트롤러 → 응답) 이해
- 결과: (실험) void 반환 시 메서드는 실행되지만 500 에러(Circular view path) / `@ResponseBody` 적용 후 `/` → "SBB", `/hello` → "안녕하세요 ^ ^" 확인

### 003-01 DB 설정, 완료, H2·JPA·개발/테스트 설정 분리
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/003-01)
- 교재: 2-03 / 강의: 14163 5~6강
- 작업: H2·H2 콘솔·JPA 의존성 추가, 설정을 공통/dev/test 3개 파일로 분리, SQL 로그·auto-commit false 설정
- 이유: 질문·답변을 저장할 DB 준비. ⭐개선: 테스트가 개발 DB를 건드리지 않도록 분리 ([개선점 1](docs/improvements.md))
- 결과: 테스트는 메모리 DB(`mem:db_test`), 서버는 파일 DB(`./db_dev`) 사용 확인, H2 콘솔 접속 확인, `db_dev.mv.db`는 git에서 제외됨

### 000-03 프로젝트 준비, 검증 방식과 코드 보기 방식 변경
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/000-03)
- 작업: PLAN.md 진행 순서 수정 (검증은 테스트로, 코드는 학습용 클론으로 보기)
- 이유: 이미 아는 동작을 매번 서버로 확인하는 건 불필요한 부담. 서버 실행은 필요할 때 직접 함
- 결과: 이후 기능부터 적용

### 004-01 엔티티, 완료, Question·Answer 엔티티와 연관관계
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/004-01)
- 교재: 2-04 / 강의: 14163 7~13강
- 작업: Question(id, createDate, subject, content, answers), Answer(id, createDate, question, content) 엔티티 추가, `@ManyToOne` / `@OneToMany(mappedBy, cascade=REMOVE)` 연관관계
- 이유: 질문·답변을 저장할 테이블 정의, 객체(서로 참조)와 테이블(외래키) 사이의 패러다임 불일치를 JPA가 어떻게 연결하는지 이해
- 결과: 테스트 SQL 로그에서 `question`, `answer(question_id FK)` 테이블 생성 확인 / (실험) `mappedBy`를 빼면 불필요한 `question_answers` 중간 테이블이 추가로 생성됨

### 005-01 BaseEntity, 시작, 공통 칸(id, createDate)을 BaseEntity로 모음
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/005-01)
- 교재: (없음, 개선) / 강의: 14168 2강
- 작업: `@MappedSuperclass` BaseEntity에 id, createDate를 두고 Question·Answer가 상속
- 이유: 엔티티마다 반복되던 코드 제거, 앞으로 늘어날 엔티티(회원, 댓글 등) 대비
- 결과: 테스트 SQL 로그의 question/answer 테이블 구조가 이전과 동일, base_entity 테이블은 생기지 않음

### 005-02 BaseEntity, JPA Auditing으로 작성일·수정일 자동 기록
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/005-02)
- 교재: (없음, 개선) / 강의: 14168 2강
- 작업: `@EnableJpaAuditing`, BaseEntity에 `@CreatedDate`·`@LastModifiedDate`(modifyDate 추가)·`@Getter`, BaseEntityTest 추가
- 이유: 교재처럼 저장할 때마다 `LocalDateTime.now()`를 직접 넣으면 깜빡할 수 있음 → JPA가 자동으로 기록
- 결과: 날짜를 넣지 않고 저장했는데 insert SQL에 create_date, modify_date 값이 자동으로 들어감 (테스트 통과)

### 005-03 BaseEntity, 완료, Setter 없이 생성자로 엔티티 만들기
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/005-03)
- 교재: (없음, 개선) / 강의: 14168 2강
- 작업: Question·Answer에 `@Getter`, `@NoArgsConstructor`, 값을 받는 생성자 추가 (Setter 없음), 개선점 2~4 기록
- 이유: Setter가 있으면 어디서든 값을 바꿀 수 있음 → 만들 때 생성자로만 값을 넣고, 변경은 이후 이름 있는 메서드로만
- 결과: `new Question("제목", "내용")`으로 저장 시 제목·내용·날짜가 모두 정상 저장 (테스트 통과)

### 006-01 리포지터리 + 테스트, 시작, QuestionRepository·AnswerRepository 추가
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-01)
- 교재: 2-05 / 강의: 14163 14강
- 작업: `JpaRepository`를 상속한 QuestionRepository, AnswerRepository 인터페이스 추가
- 이유: 엔티티를 저장/조회/수정/삭제할 창구. 인터페이스만 만들면 스프링이 구현을 자동 생성
- 결과: 앱 시작 로그에 "Found 2 JPA repository interfaces", 테스트 통과

### 006-02 리포지터리 + 테스트, 앱 시작 시 초기 데이터 생성
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-02)
- 교재: 2-05 / 강의: 14163 14강, 14168 2강
- 작업: BaseInitData 추가 (`ApplicationRunner`로 앱 시작 직후 질문 2개, 답변 1개 저장, 이미 있으면 건너뜀)
- 이유: 교재는 테스트 메서드 안에서 그때그때 저장 → 앱 시작 시 기준 데이터를 일관되게 넣어 두고 테스트가 그걸 기준으로 확인
- 결과: 테스트 로그에 question 2건, answer 1건(question_id=2) insert 확인

### 006-03 리포지터리 + 테스트, 전체 조회·id 조회 테스트
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-03)
- 교재: 2-05 / 강의: 14163 14~15강
- 작업: QuestionRepositoryTest 추가 (t1 findAll, t2 findById), AssertJ `assertThat` 사용
- 이유: 교재처럼 테스트 메서드 하나를 계속 고치지 않고, 실습마다 t1, t2...로 쌓아 학습 과정을 보존
- 결과: 테스트 통과 (전체 2건, 1번 질문 제목 확인)

### 006-04 리포지터리 + 테스트, 쿼리 메서드 (findBySubject, AndContent, Like)
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-04)
- 교재: 2-05 / 강의: 14163 16~17강
- 작업: QuestionRepository에 `findBySubject`, `findBySubjectAndContent`, `findBySubjectLike` 선언, 테스트 t3~t5
- 이유: 메서드 이름 규칙만으로 SQL이 만들어지는 쿼리 메서드 이해
- 결과: SQL 로그에서 `where subject=?`, `where subject=? and content=?`, `where subject like ?` 확인, 테스트 통과

### 006-05 리포지터리 + 테스트, 실험(실패), 삭제 테스트가 다른 테스트를 깨뜨림
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-05)
- 교재: 2-05 / 강의: 14163 18~19강
- 작업: 삭제 테스트 t7 추가, 실험을 위해 `@Order(1)`로 삭제 테스트를 강제로 먼저 실행
- 이유: 테스트가 DB를 바꾼 채로 끝나면 다른 테스트에 영향을 준다는 것을 직접 확인
- 결과: ⚠️ 테스트 실패(의도함). 삭제 후 t1(2건 기대→1건), t2~t4(No value present), t5(빈 목록) 총 5개 실패

### 006-06 리포지터리 + 테스트, @Transactional 롤백으로 테스트 독립성 확보
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-06)
- 교재: 2-05 / 강의: 14163 18강
- 작업: QuestionRepositoryTest 클래스에 `@Transactional` → 각 테스트가 끝나면 롤백
- 이유: 테스트가 남긴 변경(삭제·수정)이 다른 테스트에 영향을 주지 않게 (테스트 독립성)
- 결과: 삭제 테스트를 여전히 먼저 실행해도 6개 테스트 모두 통과 ([개선점 5](docs/improvements.md))

### 006-07 리포지터리 + 테스트, 질문 수정 modify()와 더티체킹
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-07)
- 교재: 2-05 / 강의: 14163 18강, 14168 32강
- 작업: Question에 `modify(subject, content)` 추가, 수정 테스트 t6 (save 호출 없이 flush만)
- 이유: Setter 대신 의미 있는 수정 메서드 사용. 트랜잭션 안에서 값만 바꾸면 JPA가 알아서 UPDATE하는 더티체킹 이해
- 결과: `save()`를 부르지 않았는데 SQL 로그에 `update question set ... subject=?` ← [수정된 제목] 확인, 테스트 통과

### 006-08 리포지터리 + 테스트, 답변 저장·조회 (AnswerRepository)
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-08)
- 교재: 2-05 / 강의: 14163 20강
- 작업: 테스트 t8(답변을 만들어 answerRepository.save), t10(answerRepository.findById)
- 이유: 교재 방식의 답변 저장을 먼저 익히고, 다음 커밋들에서 질문을 통해 답변을 추가하는 방식과 비교
- 결과: 답변 count 1 → 2, 1번 답변 내용 확인, 테스트 9개 통과

### 006-09 리포지터리 + 테스트, 실험(실패), addAnswer에서 NullPointerException
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-09)
- 교재: 2-05 / 강의: 14163 21~23강
- 작업: Question에 `addAnswer(content)` 추가, 테스트 t9(새 질문에 답변 추가)
- 이유: 답변 추가를 질문에게 맡기는 객체지향 방식 시도. answers 목록을 초기화하지 않으면 어떻게 되는지 확인
- 결과: ⚠️ 테스트 실패(의도함). `NullPointerException: ... because this.answers is null`

### 006-10 리포지터리 + 테스트, 실험(실패), 목록은 초기화했지만 답변이 저장되지 않음
- 날짜: 2026-11-13
- [링크](https://github.com/ldh104705-ops/jsb-plus/tree/006-10)
- 교재: 2-05 / 강의: 14163 21·23강
- 작업: answers를 `new ArrayList<>()`로 초기화, t9에 "질문만 save → 답변도 저장됐는지" 확인 추가
- 이유: NullPointerException 해결 후, 질문만 저장하면 답변도 같이 저장되는지 확인
- 결과: ⚠️ 테스트 실패(의도함). NPE는 사라졌지만 `Expecting actual: 0 to be greater than: 0` → 답변이 저장되지 않음 (cascade PERSIST 없음)
