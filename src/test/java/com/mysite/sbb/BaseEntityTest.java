package com.mysite.sbb;

import com.mysite.sbb.question.Question;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// BaseEntity의 날짜 자동 기록(Auditing)을 확인하는 테스트
@ActiveProfiles("test")
@SpringBootTest
// @Transactional : DB에 저장하려면 트랜잭션 안이어야 한다.
// 테스트에 붙이면 테스트가 끝날 때 저장한 내용을 되돌린다(롤백). → 자세한 건 006에서 실험
@Transactional
class BaseEntityTest {
    // EntityManager : JPA의 "저장 창구". 006에서 만들 리포지터리도 내부적으로 이걸 쓴다
    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("저장하면 createDate, modifyDate가 자동으로 채워진다")
    void t1() {
        // 생성자로 제목, 내용만 넣는다 (005-03). 날짜는 넣지 않는다
        Question question = new Question("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");

        // 저장 전: 날짜를 넣은 적이 없으니 비어 있다
        assertThat(question.getCreateDate()).isNull();

        em.persist(question); // 저장 (이 순간 Auditing이 날짜 도장을 찍는다)
        em.flush();           // 모아 둔 SQL을 지금 DB로 보낸다 → 로그에서 insert 확인 가능

        // 저장 후: 날짜를 직접 넣지 않았는데도 채워져 있다
        assertThat(question.getCreateDate()).isNotNull();
        assertThat(question.getModifyDate()).isNotNull();
        // 생성자로 넣은 값도 그대로 저장되어 있다
        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }
}
