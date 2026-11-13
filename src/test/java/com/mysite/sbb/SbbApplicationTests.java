package com.mysite.sbb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// @ActiveProfiles("test") : 이 테스트는 test 프로필로 실행한다
// → application-test.yaml이 적용되어 메모리 DB(mem:db_test)를 쓴다 (개발 DB와 분리)
@ActiveProfiles("test")
// @SpringBootTest : 테스트할 때 스프링부트 앱 전체를 실제로 띄운다
@SpringBootTest
class SbbApplicationTests {

    // 내용이 비어 있지만 의미가 있다.
    // 앱이 오류 없이 켜지기만 하면 통과 → "설정이 깨지지 않았다"를 확인하는 테스트
    @Test
    void contextLoads() {
    }

}
