package com.mysite.sbb.answer;

import com.mysite.sbb.BaseEntity;
import com.mysite.sbb.question.Question;
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
    //
    // fetch = LAZY : 질문은 "실제로 쓸 때" 가져온다 (지연 로딩, 006-15, 14163 24~25강)
    //   비유: 즉시 배달(EAGER) = 시키지 않은 반찬까지 매번 같이 옴
    //         필요할 때 배달(LAZY) = 반찬이 필요할 때 그때 주문
    //   [실험, 006-14] @ManyToOne 기본값(EAGER)이면 답변만 조회해도 question을 JOIN해서 같이 가져왔다
    //   LAZY로 바꾸면 답변 조회 SQL에는 answer만 나오고,
    //   answer.getQuestion().getSubject()처럼 질문을 실제로 쓰는 순간 question을 따로 조회한다
    //   (단, 그 순간에도 DB 연결이 살아 있어야 함 → 테스트 클래스의 @Transactional이 연결을 유지해 준다)
    @ManyToOne(fetch = FetchType.LAZY)
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String content;

    // 답변은 반드시 어떤 질문에 속하므로, 만들 때 질문과 내용을 함께 받는다
    public Answer(Question question, String content) {
        this.question = question;
        this.content = content;
    }
}
