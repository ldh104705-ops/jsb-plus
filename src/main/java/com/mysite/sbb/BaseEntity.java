package com.mysite.sbb;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

// BaseEntity : 모든 엔티티가 공통으로 갖는 칸(id, 작성일, 수정일)을 한 곳에 모은 부모 클래스
// 비유: "문서번호 / 작성일 / 수정일" 칸이 미리 인쇄된 서류 용지
// (005-01: Question·Answer에 똑같이 반복되던 코드를 여기로 모음, 14168 2강)
//
// @MappedSuperclass : "이 클래스는 테이블이 아니라, 자식 엔티티에게 칸만 물려주는 부모"라는 표시
//                     → base_entity 테이블은 생기지 않고, question/answer 테이블에 id, create_date 칸이 생긴다
// abstract : BaseEntity 자체로는 객체를 만들 일이 없으므로 추상 클래스로 둔다
//
// @EntityListeners(AuditingEntityListener.class) : 저장/수정되는 순간을 지켜보다가 날짜 도장을 찍어 주는 감시자를 붙인다
//   (SbbApplication의 @EnableJpaAuditing으로 도장 기능을 켜야 동작한다)
// @Getter : 모든 필드의 getter(getId(), getCreateDate() ...)를 자동 생성 → 값을 "읽기"만 가능
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
    // @Id : 행을 구분하는 번호(기본키, PK)
    // @GeneratedValue(IDENTITY) : 번호를 DB가 1, 2, 3... 자동으로 매긴다
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private int id;

    // @CreatedDate : 처음 저장될 때 현재 시각을 자동으로 넣는다
    // (교재는 저장할 때마다 q.setCreateDate(LocalDateTime.now())를 직접 호출 → 깜빡하면 빈칸)
    @CreatedDate
    private LocalDateTime createDate;

    // @LastModifiedDate : 저장/수정될 때마다 현재 시각으로 갱신한다
    // (교재는 3-09에서야 modifyDate를 추가하고, 수정할 때마다 직접 넣는다)
    @LastModifiedDate
    private LocalDateTime modifyDate;
}
