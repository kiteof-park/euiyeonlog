package com.euiyeonlog.controller;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.respository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/* 📌
* 테스트 클래스에서 @Autowired로 MockMVC를 주입받으려면, Spring 테스트 컨텍스트 로드가 필요
* 현재 코드는 일반 JUnit 테스트 클래스로 작성되어 있어 Spring 기능 사용❌
* @WebMvcTest 어노테이션을 추가해 Spring 컨텍스트가 로드되도록 설정 - 뭔말임?
* */
// @WebMvcTest(PostController.class)   // @MockMvc를 사용하기 위해 @WebMvcTest추가
@SpringBootTest                        // 컨트롤러, 서비스, 레포지토리 모두 테스트하기 위해 추가 -> @WebMvcTest는 컨트롤러 테스트만 가능
@AutoConfigureMockMvc                  // @SpringBootTest를 달면 MockMvc 주입이 안되는 문제를 @AutoConfigureMockMvc로 해결
 class PostControllerTest {

    @Autowired
    ObjectMapper objectMapper;  // ObjectMapper는 뭐하는 앨까?

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    // 📌 @BeforeEach : 테스트 메서드들이 실행되기 전에 항상 수행되도록 보장
    // 📌 모든 테스트 메서드들은 반복 수행할 수 있어야 하고, 서로 영향을 주면 안됨
    @BeforeEach
    void clean(){
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("/posts로 GET 요청 시 Hello World를 출력")
    void getTest() throws Exception {
        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/posts"))
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
        PostCreate request = PostCreate.builder()
                .title("제목입니당")
                .content("내용입니당")
                .build();

        String json = objectMapper.writeValueAsString(request);
        
        // ⌨️ static 메서드 import : alt + enter
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(APPLICATION_JSON)
                        // .content("{\"title\" : \"제목입니다\", \"content\" : \"내용입니다\"}")
                        .content(json)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                // .andExpect(MockMvcResultMatchers.content().string("Hello World"))
                .andExpect(MockMvcResultMatchers.content().string(""))
                .andDo(MockMvcResultHandlers.print());
    }

    // ♻️ 테스트 코드 리팩토링
    @Test
    @DisplayName("/posts로 POST 요청 테스크 코드 리팩토링")
    void postTestRefactor() throws Exception{
        // given
        // 생각해볼거리 : 사용자 요청 데이터는 PostCreate(DTO)로 받지 않나? -> 사용하자
        // new를 지양❌ -> new는 생성자 인수 순서를 지켜야 함, 버그 원인 발견이 어려움 -> @Builder 사용
        // PostCreate request = new PostCreate("제목이당", "내용이당");

        PostCreate request = PostCreate.builder()
                .title("제목이당 빌더")
                .content("내용이당 빌더")
                .build();

        // 필드 주입으로 ObejctMapp를 사용
        // ObjectMapper objectMapper = new ObjectMapper();

        // PostCreate를 JSON으로 가공
        String json = objectMapper.writeValueAsString(request);

        System.out.println(json);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("{}"))    // 컨트롤러에서 Map.of()를 반환
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
        // given
        PostCreate request =PostCreate.builder()
                .content("의연 최고 짱 title값 필수 테스트")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                        .contentType(APPLICATION_JSON)
                        //.content("{\"title\" : \"\", \"content\" : \"내용입니다\"}")
                        //.content("{\"title\" : null, \"content\" : \"내용입니다\"}") // ✅ @NotBlank는 null도 잡음
                        .content(json))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())

                // 📌 json데이터에 대한 검증 방법 -> jsonPath
                // .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("title을 입력해주세요"))

                // 📌 ErrorResponse에 대한 테스트
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("400"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 요청입니다."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.validation.title").value("title을 입력해주세요"))

                // .andExpect(jsonPath("$.title").value)
                .andDo(MockMvcResultHandlers.print());
    }

    // 📌 게시글 저장 테스트
    @Test
    @DisplayName("/posts 요청시 DB에 값이 저장된다.")
    void savePostTest() throws Exception{
        // given
        PostCreate request = PostCreate.builder()
                .title("의연 최공 DB저장 테스트")
                .content("의연 최공 DB저장 테스트")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());

        // then - 실제로 DB에 데이터가 저장되는지 테스트
        assertEquals(1L, postRepository.count());

        Post post = postRepository.findAll().get(0);
        assertAll(
                () -> assertEquals("의연 최공 DB저장 테스트", post.getTitle()),
                () -> assertEquals("의연 최공 DB저장 테스트", post.getContent())
        );
    }

    // 📌 글 1개 조회 테스트
    @Test
    @DisplayName("글 1개 조회")
    void test4() throws Exception{
        // given
        Post post = Post.builder()
                .title("컨트롤러 테스트 제목이에옹")
                .content("컨트롤러 테스트 내용이에옹")
                .build();
        postRepository.save(post);

        // expected(when + then)
        mockMvc.perform(MockMvcRequestBuilders.get("/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(post.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("컨트롤러 테스트 제목이에옹"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("컨트롤러 테스트 내용이에옹"))
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