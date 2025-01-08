package com.euiyeonlog.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/* 📌
* 테스트 클래스에서 @Autowired로 MockMVC를 주입받으려면, Spring 테스트 컨텍스트 로드가 필요
* 현재 코드는 일반 JUnit 테스트 클래스로 작성되어 있어 Spring 기능 사용❌
* @WebMvcTest 어노테이션을 추가해 Spring 컨텍스트가 로드되도록 설정 - 뭔말임?
* */
@WebMvcTest(PostController.class)   // @MockMvc를 사용하기 위해 @WebMvcTest추가
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("/posts로 GET 요청 시 Hello World를 출력")
    void getTest() throws Exception {
        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/post s"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Hello World"))
                //  📌 테스트에 대한 요청 summary를 출력
                .andDo(MockMvcResultHandlers.print());
    }

    // 📌 JSON 데이터 형식
    @Test
    @DisplayName("/posts로 POST 요청")
    void postTestJSON() throws Exception {
        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\" : \"제목입니다.\", \"content\" : \"내용입니다\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                // .andExpect(MockMvcResultMatchers.content().string("Hello World"))
                .andExpect(MockMvcResultMatchers.content().string("{}"))
                .andDo(MockMvcResultHandlers.print());
    }

    // 📌 x-www-urlencoded 데이터 형식
//    @Test
//    @DisplayName("/posts로 POST 요청")
//    void postTestURL() throws Exception {
//        // expected
//        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
//                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)       // application/x-www.form-urlencoded
//                        .param("title", "글 제목입니다.")
//                        .param("content", "글 내용입니다.")
//                )
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Hello World"))
//                .andDo(MockMvcResultHandlers.print());
//    }
//}

    // 📌 데이터 검증 테스트
    @Test
    @DisplayName("/posts로 POST 요청 시 title값은 필수")
    void postTestTitle() throws Exception {
        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        //.content("{\"title\" : \"\", \"content\" : \"내용입니다\"}")
                        .content("{\"title\" : null, \"content\" : \"내용입니다\"}") // ✅ @NotBlank는 null도 잡음
                )
                .andExpect(MockMvcResultMatchers.status().isOk())

                // 📌 json데이터에 대한 검증 방법 -> jsonPath
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("title을 입력해주세요"))

                // .andExpect(jsonPath("$.title").value)
                .andDo(MockMvcResultHandlers.print());
    }
}

/* 📌 MockMvc
* Spring MVC 테스트 프레임워크에서 제공하는 테스트 도구
* 실제 서버를 구동하지 않고, 웹 어플리케이션의 동작을 가상으로 테스트
* -컨트롤러 테스트(HTTP 요청(GET, POST, PUT, DELETE)를 가상으로 생성하고 컨트롤러 동작 검증
* -HTTP 상태코드, 응답 본문, 헤더 등을 쉽게 검증
* ✅ 주로 단위 테스트 시 사용
* */

/* 📌 Content-Type
* Content-Type은 HTTP헤더값
* 서버로 요청을 할 때나 요청을 받을 때
* 서버로 보내는 데이터가 어떤 데이터 형태인지,
* 서버에서 넘어온 데이터가 어떤 데이터 형태인지 명시
* */