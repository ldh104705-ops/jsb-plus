package com.mysite.sbb;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// @Entity : "이 클래스는 DB 테이블과 연결된다"는 표시
// 엑셀로 치면 시트 양식. 필드 하나 = 열 하나, 객체 하나 = 행 하나
// ddl-auto: update 설정 덕분에 JPA가 question 테이블을 자동으로 만든다
//
// extends BaseEntity : id, createDate, modifyDate는 BaseEntity에서 물려받는다
// (005-01: Question·Answer에 똑같이 반복되던 코드를 BaseEntity로 모음)
//
// @Getter : 값을 "읽는" 메서드(getSubject() 등)만 만든다. 화면에 보여줄 때 필요
// @Setter는 붙이지 않는다 (005-03, 14168 2강)
//   → setter가 있으면 코드 어디서든 아무 때나 값을 바꿀 수 있다 (예: setId(999))
//   → 값은 생성자로 "만들 때" 넣고, 바꾸는 건 나중에 modify() 같은 이름 있는 메서드로만
// @NoArgsConstructor : 빈 생성자 Question()을 만든다
//   → JPA가 DB에서 읽어 온 데이터로 객체를 만들 때 빈 생성자가 반드시 필요하다
@Entity
@Getter
@NoArgsConstructor
public class Question extends BaseEntity {
    // 제목: 최대 200자 → varchar(200)
    @Column(length = 200)
    private String subject;

    // 내용: 길이 제한 없는 글 → text
    @Column(columnDefinition = "TEXT")
    private String content;

    // @OneToMany : 질문(One) 하나에 답변(Many) 여러 개
    // mappedBy = "question" : 관계의 진짜 기록은 Answer.question(question_id 칸)에 있다.
    //                         여기는 그걸 "비춰 보는 거울"일 뿐, DB에 칸을 만들지 않는다
    // cascade = REMOVE : 질문을 지우면 달린 답변도 같이 지운다
    //
    // [실험] mappedBy를 빼면?
    //   → JPA가 관계를 두 개로 착각해서 question_answers 라는 중간 테이블을 추가로 만든다 (불필요)
    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answers;

    // 질문을 만들 때 필요한 값(제목, 내용)을 한 번에 받는 생성자
    // 교재: new Question() 후 setSubject(), setContent(), setCreateDate()를 따로 호출
    // 여기: new Question("제목", "내용") 한 줄. id·작성일은 JPA가 채운다
    public Question(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    // 질문 수정 (006-07)
    // setSubject(), setContent()처럼 아무 칸이나 바꾸는 문을 여는 대신,
    // "질문을 수정한다"는 의미 있는 행동 하나만 허용한다
    // (원래 028 질문 수정에서 만들 예정이었지만, 006의 수정 테스트에 필요해서 먼저 만듦)
    public void modify(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }
}
