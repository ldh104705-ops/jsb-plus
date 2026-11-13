package com.mysite.sbb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// QuestionRepositoryTest : 리포지터리의 저장/조회/수정/삭제를 하나씩 확인하는 학습용 테스트
// (교재는 SbbApplicationTests의 메서드 하나를 계속 고쳐 가며 실습 → 이전 실습이 사라짐
//  이 프로젝트는 14163처럼 대상별 클래스 + t1, t2... 로 실습 과정을 테스트로 쌓아 둔다)
//
// 기준 데이터(BaseInitData): 1번 질문 "sbb가 무엇인가요?", 2번 질문 "스프링부트 모델 질문입니다." + 답변 1개
@ActiveProfiles("test")
@SpringBootTest
// JUnit은 테스트 실행 순서를 보장하지 않는다. (오늘은 마지막이었던 테스트가 내일은 처음일 수 있다)
// 실험을 위해 @Order가 붙은 테스트(삭제 t7)를 "강제로 먼저" 실행시킨다
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("findAll : 전체 조회")
    void t1() {
        // SELECT * FROM question
        List<Question> questions = questionRepository.findAll();

        // AssertJ : assertThat(실제값).isEqualTo(기대값) 처럼 영어 문장으로 읽힌다
        // (교재는 JUnit의 assertEquals(기대값, 실제값) → 순서가 헷갈리기 쉽다)
        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findById : id로 하나 조회")
    void t2() {
        // SELECT * FROM question WHERE id = 1
        // findById는 Optional을 돌려준다 (없을 수도 있으니까). get()으로 꺼낸다
        Question question = questionRepository.findById(1).get();

        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findBySubject : 제목으로 조회 (쿼리 메서드)")
    void t3() {
        // SELECT * FROM question WHERE subject = 'sbb가 무엇인가요?'
        Question question = questionRepository.findBySubject("sbb가 무엇인가요?").get();

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectAndContent : 제목 + 내용으로 조회")
    void t4() {
        // SELECT * FROM question WHERE subject = ? AND content = ?
        Question question = questionRepository
                .findBySubjectAndContent("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.")
                .get();

        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectLike : 제목이 'sbb'로 시작하는 질문 조회")
    void t5() {
        // SELECT * FROM question WHERE subject LIKE 'sbb%'
        // % : 아무 글자나 0개 이상 → "sbb%" = sbb로 시작하는 모든 제목
        List<Question> questions = questionRepository.findBySubjectLike("sbb%");

        assertThat(questions.get(0).getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    // [실험(실패)] 삭제 테스트가 먼저 실행되면?
    //   → 1번 질문이 DB에서 "진짜로" 지워진 채로 남는다 (되돌리는 장치가 없음)
    //   → 1번 질문이 있다고 믿는 t1~t5가 줄줄이 실패한다
    @Test
    @Order(1)
    @DisplayName("delete : 삭제")
    void t7() {
        assertThat(questionRepository.count()).isEqualTo(2);

        // DELETE FROM question WHERE id = 1
        Question question = questionRepository.findById(1).get();
        questionRepository.delete(question);

        assertThat(questionRepository.count()).isEqualTo(1);
    }
}
