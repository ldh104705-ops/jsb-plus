package com.mysite.sbb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 답변 엔티티 → DB의 answer 테이블
//
// extends BaseEntity : id, createDate, modifyDate는 BaseEntity에서 물려받는다
// (005-01: Question·Answer에 똑같이 반복되던 코드를 BaseEntity로 모음)
//
// @Getter만 두고 @Setter는 두지 않는다 (005-03, 이유는 Question.java 참고)
// @NoArgsConstructor : JPA가 DB 데이터로 객체를 만들 때 필요한 빈 생성자
@Entity
@Getter
@NoArgsConstructor
public class Answer extends BaseEntity {
    // @ManyToOne : 답변(Many) 여러 개가 질문(One) 하나에 속한다
    // → answer 테이블에 question_id 칸(외래키, FK)이 생긴다
    // → 이 칸이 질문-답변 관계의 "진짜 기록"이다 (관계의 주인)
    @ManyToOne
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 답변은 반드시 어떤 질문에 속하므로, 만들 때 질문과 내용을 함께 받는다
    public Answer(Question question, String content) {
        this.question = question;
        this.content = content;
    }
}
