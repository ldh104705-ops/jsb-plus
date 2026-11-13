package com.mysite.sbb;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// BaseInitData : 앱이 켜질 때 기본 데이터(질문 2개, 답변 1개)를 넣어 두는 클래스
// 비유: 가게 문 열기 전에 진열대에 샘플 상품을 올려 두기
// → 개발 중 화면 확인용, 테스트에서 "이 데이터가 있다"고 믿고 확인하는 기준 데이터
// (교재는 테스트 메서드 안에서 그때그때 저장 → 이 프로젝트는 14163/14168처럼 앱 시작 시 자동 생성)
//
// @Configuration : 스프링 설정 클래스. 앱이 켜질 때 스프링이 읽어 들인다
// @RequiredArgsConstructor : final 필드를 받는 생성자를 Lombok이 만들어 준다
//   → 스프링이 그 생성자로 리포지터리를 넣어 준다 (생성자 주입)
@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    // ApplicationRunner : "앱이 다 켜진 직후에 실행할 일"
    // @Bean으로 등록하면 스프링이 앱 시작이 끝난 뒤 자동으로 실행한다
    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        return args -> work1();
    }

    void work1() {
        // 이미 데이터가 있으면 또 넣지 않는다 (개발 DB는 파일이라 서버를 다시 켜도 남아 있음)
        if (questionRepository.count() > 0) return;

        Question q1 = new Question("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
        questionRepository.save(q1); // 1번 질문

        Question q2 = new Question("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?");
        questionRepository.save(q2); // 2번 질문

        answerRepository.save(new Answer(q2, "네 자동으로 생성됩니다.")); // 2번 질문의 1번 답변
    }
}
