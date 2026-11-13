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
