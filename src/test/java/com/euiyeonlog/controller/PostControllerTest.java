package com.euiyeonlog.controller;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.request.PostEdit;
import com.euiyeonlog.respository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

//    @Test
//    @DisplayName("/posts로 GET 요청 시 Hello World를 출력")
//    void getTest() throws Exception {
//        // expected
//        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts"))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.content().string("Hello World"))
//                //  📌 테스트에 대한 요청 summary를 출력
//                .andDo(MockMvcResultHandlers.print());
//    }

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
        // mockMvc.perform().content() : .content()는 요청 본문(RequestBody)에 데이터를 담기 위해 사용하는 메서드
        mockMvc.perform(MockMvcRequestBuilders.post("/euiyeonlog/posts")
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
        mockMvc.perform(MockMvcRequestBuilders.post("/euiyeonlog/posts")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(""))    // 컨트롤러에서 Map.of()를 반환
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
        mockMvc.perform(MockMvcRequestBuilders.post("/euiyeonlog/posts")
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
        mockMvc.perform(MockMvcRequestBuilders.post("/euiyeonlog/posts")
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
    void test1() throws Exception{
        // given
        Post post = Post.builder()
                .title("123456789012345")
                .content("컨트롤러 테스트 내용이에옹")
                .build();
        postRepository.save(post);

        // 클라이언트 요구사항
            // 'JSON 응답에서 title값 길이를 최대 10글자로 해주세요' 라고 한다면?
            // Post(엔티티)에서 getTitle(){return substring()} 메서드 추가 -> Jackson이 10글자만 가져감
            // 응답 클래스를 분리

        // expected(when + then)
        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(post.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("1234567890"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").value("컨트롤러 테스트 내용이에옹"))
                .andDo(MockMvcResultHandlers.print());
    }

    // 📌 글 전체 조회 테스트
    @Test
    @DisplayName("글 전체조회")
    void test2() throws Exception{
        // given
//        List<Post> posts = new ArrayList<>();
//
//        Post post1 = Post.builder()
//                .title("의연 제목")
//                .content("의연 내용")
//                .build();
//
//        Post post2 = Post.builder()
//                .title("한얼 제목")
//                .content("한얼 내용")
//                .build();
//
//        Post post3 = Post.builder()
//                .title("소윤 제목")
//                .content("소윤 내용")
//                .build();
//
//        Post post4 = Post.builder()
//                .title("진호 제목")
//                .content("진호 내용")
//                .build();
//
//        Post post5 = Post.builder()
//                .title("병중 제목")
//                .content("병중 내용")
//                .build();
//
//        posts.add(post1);   posts.add(post2);   posts.add(post3);   posts.add(post4);   posts.add(post5);
//        postRepository.saveAll(posts);

        // ♻️ 테스트 코드 리팩토링
        Post post1 = postRepository.save(Post.builder()
                .title("의연 제목1")
                .content("의연 내용1")
                .build());

        Post post2 = postRepository.save(Post.builder()
                .title("의연 제목2")
                .content("의연 내용2")
                .build());

        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts")
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                /* 📌 단건 조회인 경우 Json Obejct가 응답으로 왔었음
                    {"id" : "1","title" : "어쩌구" ...}

                    📌 전체 조회인 경우 리스트 형식으로 응답이 내려옴
                     [{...}, {...}, {...}]
                 */

                /*  📌 실제 응답
                        Body = [
                        {"id":1,"title":"의연 제목","content":"의연 내용"},
                        {"id":2,"title":"한얼 제목","content":"한얼 내용"},
                        {"id":3,"title":"소윤 제목","content":"소윤 내용"},
                        {"id":4,"title":"진호 제목","content":"진호 내용"},
                        {"id":5,"title":"병중 제목","content":"병중 내용"}
                        ]
                */

                // 📌 검증 포인트 - json 응답 리스트의 길이, json 응답 객체의 값(title, content 등)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(post1.getId()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(post1.getTitle()))
//                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value(post1.getContent()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("의연 제목1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("의연 내용1"))
                .andDo(MockMvcResultHandlers.print());
    }

    // 📌 글 전체 조회 테스트 - 페이징 처리
    @Test
    @DisplayName("글 전체 조회 - 페이징 처리")
    void test3() throws Exception{
        // given
        List<Post> requestPosts = IntStream.range(1, 31)     // 스트림에 정수 i가 하나씩 전달
                // .mapToObj(i -> {...}) : i를 입력을 받아 동작 수행
                // 각 요소(int)를 객체로 변환, Stream<Post>에 포함
                .mapToObj(i -> Post.builder()
                                .title("의연 미래 - " + i)
                                .content("월 1000만원 - " + i)
                                .build())
                .collect(Collectors.toList());              // Post 객체를 리스트로 수집

        postRepository.saveAll(requestPosts);
        System.out.println(requestPosts);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts?page=1&sort=id,desc&size=5")
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(requestPosts.get(0).getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(requestPosts.get(0).getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value(requestPosts.get(0).getTitle()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("글 여러 개 조회 - PostSerach")
    void test4() throws Exception{
        // given
        List<Post> posts = IntStream.range(1, 21)
                .mapToObj(i -> Post.builder()
                        .title("의연 제목 - " + i)
                        .content("의연 내용 - " + i)
                        .build())
                .toList();

        postRepository.saveAll(posts);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts?page=2&size=10")
                        .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(10)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value("의연 제목 - 10"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("의연 내용 - 10"))
                .andDo(MockMvcResultHandlers.print());
    }


    // 📌 글 내용 업데이트 테스트
    @Test
    @DisplayName("글 제목 수정")
    void test5() throws Exception{
        // given
        Post post = getPost("의연", "반포자이");
        postRepository.save(post);

        PostEdit postEdit = PostEdit.builder()
                .title("의연")
                .content("초가집")
                .build();

        mockMvc.perform(MockMvcRequestBuilders.patch("/euiyeonlog/posts/{postId}", post.getId())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    private Post getPost(String title, String content) {
        return Post.builder()
                .title(title)
                .content(content)
                .build();
    }

    // 📌 게시글 삭제 테스트
    @Test
    @DisplayName("게시글 삭제")
    void test6() throws Exception{
        // given
        Post post = Post.builder()
                .title("의연 짱짱")
                .content("의연 최고 짱짱")
                .build();
        postRepository.save(post);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.delete("/euiyeonlog/posts/{postsId}", post.getId())
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회")
    void test7() throws Exception{

        // expected
        mockMvc.perform(MockMvcRequestBuilders.get("/euiyeonlog/posts/{postId}", 1L)
                .contentType(APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글 수정")
    void test8() throws Exception{
        PostEdit postEdit = PostEdit.builder()
                .title("의연")
                .content("초가집")
                .build();

        // expected
        mockMvc.perform(MockMvcRequestBuilders.patch("/euiyeonlog/posts/{postId}", 1L)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postEdit)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }
    
    @Test
    @DisplayName("제목에 '바보'가 포함된 게시글 작성")
    void test9() throws Exception{
        PostCreate postCreate = PostCreate.builder()
                .title("바보의연")
                .content("천재의연")
                .build();

        String requestJson = objectMapper.writeValueAsString(postCreate);

        mockMvc.perform(MockMvcRequestBuilders.post("/euiyeonlog/posts")
                        .contentType(APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
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