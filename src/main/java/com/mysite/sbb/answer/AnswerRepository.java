package com.mysite.sbb.answer;

import org.springframework.data.jpa.repository.JpaRepository;

// AnswerRepository : 답변을 DB에 저장/조회/수정/삭제하는 창고 관리인
// (QuestionRepository와 같은 방식. 기본 메서드는 JpaRepository가 제공)
public interface AnswerRepository extends JpaRepository<Answer, Integer> {
}
