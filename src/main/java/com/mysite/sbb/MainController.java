package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// @Controller : "이 클래스는 요청을 받아 처리하는 컨트롤러"라는 표시
// 스프링이 앱을 켤 때 자동으로 찾아서 등록한다. (SbbApplication 하위 패키지에 있으므로)
@Controller
public class MainController {

    // @GetMapping("/") : 브라우저가 GET 방식으로 "/" 주소에 접속하면 이 메서드를 실행한다 (= 메뉴판)
    // 요청은 먼저 DispatcherServlet(카운터 직원)이 받고, 메뉴판을 보고 이 메서드로 넘겨준다.
    @GetMapping("/")
    // @ResponseBody : 반환한 문자열을 "그대로" 브라우저에 응답한다.
    // 이게 없으면 스프링은 반환값을 "화면 파일(템플릿) 이름"으로 해석한다.
    //
    // [실험] 반환 타입을 void로 하고 @ResponseBody를 빼면?
    //   → 메서드는 실행되지만(콘솔 출력 확인), 응답할 내용이 없어서
    //     스프링이 URL 이름("hello")을 화면 이름으로 추측 → 화면 파일이 없어 500 에러
    //     (에러 메시지: Circular view path [hello])
    @ResponseBody
    public String index() {
        return "SBB";
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        return "안녕하세요 ^ ^";
    }
}
