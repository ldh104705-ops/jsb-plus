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
