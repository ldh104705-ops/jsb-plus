package com.mysite.sbb;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

// @Entity : "이 클래스는 DB 테이블과 연결된다"는 표시
// 엑셀로 치면 시트 양식. 필드 하나 = 열 하나, 객체 하나 = 행 하나
// ddl-auto: update 설정 덕분에 JPA가 question 테이블을 자동으로 만든다
@Entity
public class Question {
    // @Id : 행을 구분하는 번호(기본키, PK)
    // @GeneratedValue(IDENTITY) : 번호를 DB가 1, 2, 3... 자동으로 매긴다
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    private LocalDateTime createDate;

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
}
