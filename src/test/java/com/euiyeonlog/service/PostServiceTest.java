package com.euiyeonlog.service;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.request.PostEdit;
import com.euiyeonlog.response.PostResponse;
import com.euiyeonlog.respository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PostServiceTest {

    // @SpringBootTest를 생략하면 주입이 안되는 이유가 몰까 ..?
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void clean(){
        postRepository.deleteAll();
    }

    @Test
    @DisplayName("글 작성 테스트")
    void test1(){
        // given
        PostCreate postCreate = PostCreate.builder()
                .title("제목이다옹")
                .content("내용이다옹")
                .build();

        // when
        postService.write(postCreate);

        // then
        Post post = postRepository.findAll().get(0);
        assertAll(
                () -> assertEquals(1L, postRepository.count()),
                () -> assertEquals("제목이다옹", post.getTitle()),
                () -> assertEquals("내용이다옹", post.getContent())
        );
    }
    
    // 단건 조회 테스트 -> 우선 글을 먼저 저장해주는 작업이 필요
    @Test
    @DisplayName("글 1개 조회")
    void test2(){
        // given
        Post requestPost = Post.builder()
                .title("호돌맨 제목 테스트")
                .content("호돌맨 내용 테스트")
                .build();

         postRepository.save(requestPost);

        // when
        PostResponse postResponse = postService.get(requestPost.getId());

        // then
        assertAll(
                () -> assertNotNull(postResponse),
                // ⚠️ test1()에 의해 Test Failed -> @BeforeEach를 활용
                () -> assertEquals(1L, postRepository.count()),
                () -> assertEquals("호돌맨 제목 테스트", postResponse.getTitle()),
                () -> assertEquals("호돌맨 내용 테스트", postResponse.getContent())
        );
    }

    // 전체 조회 테스트 -> 우선 글을 먼저 저장해주는 작업이 필요
    @Test
    @DisplayName("글 1개 조회")
    void test3(){
//        List<Post> posts = new ArrayList<>();
//
//        // given
//        Post requestPost1 = Post.builder()
//                .title("의연 제목 테스트")
//                .content("의연 내용 테스트")
//                .build();
//
//        Post requestPost2 = Post.builder()
//                .title("한얼 제목 테스트")
//                .content("한얼 내용 테스트")
//                .build();
//
//        Post requestPost3 = Post.builder()
//                .title("소윤 제목 테스트")
//                .content("소윤 내용 테스트")
//                .build();
//
//        Post requestPost4 = Post.builder()
//                .title("진호 제목 테스트")
//                .content("진호 내용 테스트")
//                .build();
//
//        Post requestPost5 = Post.builder()
//                .title("병중 제목 테스트")
//                .content("병중 내용 테스트")
//                .build();
//
//        posts.add(requestPost1);    posts.add(requestPost2);    posts.add(requestPost3);
//        posts.add(requestPost4);    posts.add(requestPost5);
//
//        postRepository.saveAll(posts);


        // ♻️ 테스트 코드 리팩토링
        postRepository.saveAll(List.of(
                Post.builder()
                        .title("의연 제목1")
                        .content("의연 내용1")
                        .build(),
                Post.builder()
                        .title("한얼 제목1")
                        .content("한얼 제목2")
                        .build()
        ));

        // when
        List<PostResponse> postResponses = postService.getAll();

        // then
//        assertAll();
        assertEquals(2, postResponses.size());

    }

    // 업데이트 테스트 - 제목 수정 테스트
    @Test
    @DisplayName("글 제목 수정")
    void test4(){
        // given
        Post post = Post.builder()
                        .title("의연")
                        .content("부자될랭")
                        .build();
        postRepository.save(post);

        // 수정할 데이터
        PostEdit postEdit = PostEdit.builder()
                .title("호돌맨")
                .content("부자될랭")
                .build();

        // when
        postService.edit(post.getId(), postEdit);

        // then
        Post changedPost = postRepository.findById(post.getId())
                .orElseThrow(()-> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));

        System.out.println(changedPost.getTitle());
        System.out.println(changedPost.getContent());
        assertEquals("호돌맨", changedPost.getTitle());
    }

    // 업데이트 테스트 - 내용 수정 테스트
    @Test
    @DisplayName("글 내용 수정")
    void test5(){
        // given
        Post post = Post.builder()
                .title("의연")
                .content("초가집")
                .build();
        postRepository.save(post);

        // 수정할 데이터
        PostEdit postEdit = PostEdit.builder()
                .title("호돌맨")
                .content("반포자이")
                .build();

        // when
        postService.edit(post.getId(), postEdit);

        // then
        Post changedPost = postRepository.findById(post.getId())
                .orElseThrow(()-> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));

        System.out.println(changedPost.getTitle());
        System.out.println(changedPost.getContent());
        assertEquals("반포자이", changedPost.getContent());
    }


    // 업데이트 테스트 - 수정이 필요하지 않은 필드에 null이 들어온다면?
    @Test
    @DisplayName("글 내용 수정")
    void test10(){
        // given
        Post post = Post.builder()
                .title("의연")
                .content("초가집")
                .build();
        postRepository.save(post);

        // 수정할 데이터
        PostEdit postEdit = PostEdit.builder()
                .title(null)
                .content("반포자이")
                .build();

        // when
        postService.edit(post.getId(), postEdit);

        // then
        Post changedPost = postRepository.findById(post.getId())
                .orElseThrow(()-> new RuntimeException("글이 존재하지 않습니다. id = " + post.getId()));

        System.out.println(changedPost.getTitle());
        System.out.println(changedPost.getContent());
        assertEquals("의연", changedPost.getTitle());
        assertEquals("반포자이", changedPost.getContent());
    }
    
    // 삭제 테스트
    @Test
    @DisplayName("게시글 삭제")
    void test11(){
        // given - 게시글 등록
        Post post = Post.builder()
                .title("의연 최고")
                .content("의연 최고 짱짱")
                .build();
        postRepository.save(post);

        // when -  게시글 삭제
        postService.delete(post.getId());

        //then - 레포지토리 게시글 개수 확인
        assertEquals(0, postRepository.count());

    }
}