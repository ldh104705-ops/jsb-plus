package com.mysite.sbb;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// QuestionRepository : 질문을 DB에 저장/조회/수정/삭제하는 "창고 관리인"
//
// interface만 만들면 스프링이 앱을 켤 때 실제 동작하는 구현 객체를 자동으로 만들어 준다.
// JpaRepository<Question, Integer> : "Question 엔티티를 다루고, id 타입은 Integer"
//   → save(), findAll(), findById(), count(), delete() 같은 기본 메서드가 공짜로 생긴다
//   (id 필드는 int지만, 제네릭에는 기본형을 못 쓰므로 Integer로 적는다)
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // 쿼리 메서드 : 메서드 이름을 규칙대로 지으면 스프링이 이름을 읽고 SQL을 만들어 준다 (구현 코드 없음)
    //   findBy + 필드명 → WHERE 필드 = ?

    // SELECT * FROM question WHERE subject = ?
    // 결과가 없을 수도 있으니 Optional로 받는다
    Optional<Question> findBySubject(String subject);

    // SELECT * FROM question WHERE subject = ? AND content = ?
    Optional<Question> findBySubjectAndContent(String subject, String content);

    // SELECT * FROM question WHERE subject LIKE ?
    // 여러 개가 나올 수 있으니 List로 받는다. 호출할 때 "sbb%"처럼 % 를 직접 넣는다
    List<Question> findBySubjectLike(String subjectLike);
}
