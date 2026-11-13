package com.mysite.sbb;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

// BaseEntity : 모든 엔티티가 공통으로 갖는 칸(id, 작성일)을 한 곳에 모은 부모 클래스
// 비유: "문서번호 / 작성일" 칸이 미리 인쇄된 서류 용지
// (005-01: Question·Answer에 똑같이 반복되던 코드를 여기로 모음, 14168 2강)
//
// @MappedSuperclass : "이 클래스는 테이블이 아니라, 자식 엔티티에게 칸만 물려주는 부모"라는 표시
//                     → base_entity 테이블은 생기지 않고, question/answer 테이블에 id, create_date 칸이 생긴다
// abstract : BaseEntity 자체로는 객체를 만들 일이 없으므로 추상 클래스로 둔다
@MappedSuperclass
public abstract class BaseEntity {
    // @Id : 행을 구분하는 번호(기본키, PK)
    // @GeneratedValue(IDENTITY) : 번호를 DB가 1, 2, 3... 자동으로 매긴다
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    private LocalDateTime createDate;
}
