package com.mysite.sbb;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// QuestionRepositoryTest : 리포지터리의 저장/조회/수정/삭제를 하나씩 확인하는 학습용 테스트
// (교재는 SbbApplicationTests의 메서드 하나를 계속 고쳐 가며 실습 → 이전 실습이 사라짐
//  이 프로젝트는 14163처럼 대상별 클래스 + t1, t2... 로 실습 과정을 테스트로 쌓아 둔다)
//
// 기준 데이터(BaseInitData): 1번 질문 "sbb가 무엇인가요?", 2번 질문 "스프링부트 모델 질문입니다." + 답변 1개
@ActiveProfiles("test")
@SpringBootTest
// @Transactional : 각 테스트를 트랜잭션 안에서 실행하고, 끝나면 "롤백"(없던 일로)한다
//   비유: 연습장에 쓰고 테스트가 끝나면 지우개로 지운다 → 다음 테스트는 늘 깨끗한 상태에서 시작
//   → 삭제/수정 테스트가 다른 테스트에 영향을 주지 않는다 (테스트 독립성)
//   (006-05 실험: 이게 없으면 삭제 테스트가 먼저 돌 때 다른 테스트 5개가 실패했다)
@Transactional
// JUnit은 테스트 실행 순서를 보장하지 않는다. (오늘은 마지막이었던 테스트가 내일은 처음일 수 있다)
// 삭제 테스트(t7)를 일부러 먼저 실행시켜도 다른 테스트가 통과하는지 계속 확인하기 위해 남겨 둔다
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

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

    @Test
    @DisplayName("modify : 수정 (더티체킹)")
    void t6() {
        Question question = questionRepository.findById(1).get();

        // 값만 바꾼다. save()를 부르지 않는다!
        question.modify("수정된 제목", "수정된 내용");

        // flush : 지금까지 바뀐 내용을 SQL로 만들어 DB에 보낸다 (로그에서 UPDATE를 보기 위해 호출)
        // 더티체킹 : JPA는 처음 조회했을 때의 모습을 기억해 두었다가,
        //           flush/커밋 시점에 지금 모습과 비교해서 바뀐 게 있으면 UPDATE를 알아서 보낸다
        //   비유: 문서 편집기의 자동 저장. 고치기만 하면 저장 버튼을 안 눌러도 반영된다
        // (교재는 수정 후 questionRepository.save(q)를 다시 호출)
        questionRepository.flush();

        Question foundQuestion = questionRepository.findBySubject("수정된 제목").get();
        assertThat(foundQuestion.getContent()).isEqualTo("수정된 내용");
    }

    // [실험(실패), 006-05] 롤백 없이 삭제 테스트가 먼저 실행되면?
    //   → 1번 질문이 DB에서 "진짜로" 지워진 채로 남아, 1번 질문이 있다고 믿는 t1~t5가 줄줄이 실패했다
    // [해결, 006-06] 클래스에 @Transactional → 이 테스트가 끝나면 삭제가 롤백되어 1번 질문이 돌아온다
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

    @Test
    @DisplayName("답변 저장 (AnswerRepository로)")
    void t8() {
        Question question = questionRepository.findById(2).get();

        // 답변을 만들어 AnswerRepository로 직접 저장하는 방식 (교재 방식)
        // INSERT INTO answer (content, question_id, ...) VALUES ('...', 2, ...)
        Answer answer = new Answer(question, "네 자동으로 생성됩니다.");
        answerRepository.save(answer);

        // 기준 데이터의 답변 1개 + 방금 저장한 1개
        assertThat(answerRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("답변 추가 (question.addAnswer로)")
    void t9() {
        // 새 질문을 만들고, 질문에게 답변을 달게 한다
        Question question = new Question("새 질문", "새 질문 내용");
        question.addAnswer("새 답변");

        assertThat(question.getAnswers()).hasSize(1);
    }

    @Test
    @DisplayName("답변 조회 (AnswerRepository로)")
    void t10() {
        // SELECT * FROM answer WHERE id = 1
        Answer answer = answerRepository.findById(1).get();

        assertThat(answer.getContent()).isEqualTo("네 자동으로 생성됩니다.");
    }
}
