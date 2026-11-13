package com.mysite.sbb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @SpringBootApplication : "여기가 스프링부트 앱의 시작점"이라는 표시
// 이 클래스가 있는 패키지(com.mysite.sbb)와 그 하위 패키지를 뒤져서
// 컨트롤러 같은 부품들을 자동으로 찾아 등록한다.
@SpringBootApplication
// @EnableJpaAuditing : JPA 날짜 자동 기록(Auditing) 기능을 켠다
// → BaseEntity의 @CreatedDate, @LastModifiedDate가 동작한다
@EnableJpaAuditing
public class SbbApplication {

    // 앱의 시작 버튼. 실행하면 내장 서버(톰캣)가 켜진다.
    public static void main(String[] args) {
        SpringApplication.run(SbbApplication.class, args);
    }

}
