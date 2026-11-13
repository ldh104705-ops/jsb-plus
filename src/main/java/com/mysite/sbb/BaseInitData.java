package com.mysite.sbb;

import com.mysite.sbb.question.Question;
import com.mysite.sbb.question.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

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

    // self : "스프링이 만든 나 자신의 대리인(프록시)" (006-13, 14163 27강 / 14168 33강)
    //   스프링은 @Transactional이 붙은 객체 앞에 대리인을 세운다.
    //   밖에서 대리인을 통해 메서드를 부르면 → 대리인이 트랜잭션 시작 → 진짜 메서드 실행 → 트랜잭션 종료
    //   비유: 비서를 통해 사장님께 일을 맡겨야 비서가 일정(트랜잭션)을 관리해 준다.
    //        사장님 방 안에서 혼잣말로 일을 하면(this.work1()) 비서는 모른다.
    // @Lazy : 나 자신을 주입받는 것이라, 앱 시작 시점이 아니라 "처음 쓸 때" 연결하도록 미룬다
    //         (안 그러면 "내가 만들어지려면 내가 필요한" 순환 문제가 생긴다)
    @Autowired
    @Lazy
    private BaseInitData self;

    // ApplicationRunner : "앱이 다 켜진 직후에 실행할 일"
    // @Bean으로 등록하면 스프링이 앱 시작이 끝난 뒤 자동으로 실행한다
    @Bean
    public ApplicationRunner baseInitDataApplicationRunner() {
        // work1()이 아니라 self.work1() → 대리인을 거쳐서 호출 → @Transactional 적용
        return args -> self.work1();
    }

    // @Transactional : work1 전체를 트랜잭션 하나로 묶는다 (전부 저장되거나, 전부 안 되거나)
    //
    // [실험, 006-12] 같은 클래스 안에서 work1()을 직접 부르면 @Transactional이 적용될까?
    //   → 적용되지 않았다. 트랜잭션 로그에 BaseInitData.work1이 나오지 않고,
    //     count(), save()가 각자 따로 트랜잭션을 열었다
    // [해결, 006-13] self.work1()으로 대리인을 거쳐 호출 → 로그에 BaseInitData.work1 트랜잭션이 보인다
    @Transactional
    void work1() {
        // 이미 데이터가 있으면 또 넣지 않는다 (개발 DB는 파일이라 서버를 다시 켜도 남아 있음)
        if (questionRepository.count() > 0) return;

        Question q1 = new Question("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
        questionRepository.save(q1); // 1번 질문

        Question q2 = new Question("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?");
        // 006-12: AnswerRepository 대신 질문에게 답변을 달게 한다 (cascade PERSIST로 함께 저장)
        q2.addAnswer("네 자동으로 생성됩니다.");
        questionRepository.save(q2); // 2번 질문 + 1번 답변
    }
}
