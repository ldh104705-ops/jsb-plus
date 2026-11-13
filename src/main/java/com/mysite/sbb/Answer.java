package com.mysite.sbb;

import jakarta.persistence.*;

// 답변 엔티티 → DB의 answer 테이블
//
// extends BaseEntity : id, createDate는 BaseEntity에서 물려받는다
// (005-01: Question·Answer에 똑같이 반복되던 코드를 BaseEntity로 모음)
@Entity
public class Answer extends BaseEntity {
    // @ManyToOne : 답변(Many) 여러 개가 질문(One) 하나에 속한다
    // → answer 테이블에 question_id 칸(외래키, FK)이 생긴다
    // → 이 칸이 질문-답변 관계의 "진짜 기록"이다 (관계의 주인)
    @ManyToOne
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;
}
