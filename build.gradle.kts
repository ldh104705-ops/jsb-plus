// build.gradle.kts : 빌드 설명서 (어떤 라이브러리를 쓰고, Java 몇 버전으로 빌드할지)
// Kotlin 문법으로 쓴 Gradle 설정 파일이다. (교재는 Groovy 문법의 build.gradle)

plugins {
    java
    // 스프링부트 버전
    id("org.springframework.boot") version "4.1.2"
    // 라이브러리 버전을 스프링부트에 맞춰 자동으로 정해 주는 플러그인
    // → 아래 dependencies에 버전을 따로 적지 않아도 된다
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.mysite"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        // 이 프로젝트는 Java 25로 빌드한다 (PC의 기본 Java가 달라도 25를 찾아서 쓴다)
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    // 라이브러리를 내려받을 저장소
    mavenCentral()
}

dependencies {
    // 웹 기능 (컨트롤러, 내장 톰캣 서버). Boot 4에서 starter-web → starter-webmvc로 이름이 바뀜
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Lombok: @Getter 같은 어노테이션으로 반복 코드를 자동 생성
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // DevTools: 코드를 고치면 서버를 자동 재시작 (개발할 때만 사용)
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // 테스트용 라이브러리
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    // 테스트는 JUnit 5로 실행
    useJUnitPlatform()
}
