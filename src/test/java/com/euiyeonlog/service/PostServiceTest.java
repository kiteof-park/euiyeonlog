package com.euiyeonlog.service;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.respository.PostRepository;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    
    // 조회 테스트 -> 우선 글을 먼저 저장해주는 작업이 필요
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
        Post post = postService.get(requestPost.getId());

        // then
        assertAll(
                () -> assertNotNull(post),
                // ⚠️ test1()에 의해 Test Failed -> @BeforeEach를 활용
                () -> assertEquals(1L, postRepository.count()),
                () -> assertEquals("호돌맨 제목 테스트", post.getTitle()),
                () -> assertEquals("호돌맨 내용 테스트", post.getContent())
        );
    }
}