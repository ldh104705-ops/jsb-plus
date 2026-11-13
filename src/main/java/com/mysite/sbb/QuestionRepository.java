package com.mysite.sbb;

import org.springframework.data.jpa.repository.JpaRepository;

// QuestionRepository : 질문을 DB에 저장/조회/수정/삭제하는 "창고 관리인"
//
// interface만 만들면 스프링이 앱을 켤 때 실제 동작하는 구현 객체를 자동으로 만들어 준다.
// JpaRepository<Question, Integer> : "Question 엔티티를 다루고, id 타입은 Integer"
//   → save(), findAll(), findById(), count(), delete() 같은 기본 메서드가 공짜로 생긴다
//   (id 필드는 int지만, 제네릭에는 기본형을 못 쓰므로 Integer로 적는다)
public interface QuestionRepository extends JpaRepository<Question, Integer> {
}
