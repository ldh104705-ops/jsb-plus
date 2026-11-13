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
