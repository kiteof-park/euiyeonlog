package com.euiyeonlog.service;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.exception.PostNotFound;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.request.PostEdit;
import com.euiyeonlog.request.PostSearch;
import com.euiyeonlog.response.PostResponse;
import com.euiyeonlog.respository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        Post post = Post.builder()
                .title("호돌맨 제목 테스트")
                .content("호돌맨 내용 테스트")
                .build();

         postRepository.save(post);

        // when
        PostResponse postResponse = postService.get(post.getId());

        // then
        assertAll(
                () -> assertNotNull(postResponse),
                // ⚠️ test1()에 의해 Test Failed -> @BeforeEach를 활용
                () -> assertEquals(1L, postRepository.count()),
                () -> assertEquals("호돌맨 제목 테스트", postResponse.getTitle()),
                () -> assertEquals("호돌맨 내용 테스트", postResponse.getContent())
        );
    }

    // 단건 조회 테스트 - 예외 처리
    @Test
    @DisplayName("글 1개 조회 - 존재하지 않는 글 조회")
    void test3(){
        // given
        Post post = Post.builder()
                .title("호돌맨 제목 테스트")
                .content("호돌맨 내용 테스트")
                .build();

        postRepository.save(post);

        // expected
//        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
//            postService.get(post.getId() + 1L);
//        }, "예외처리가 잘못 되었어요!");

        // 예외에 대한 메세지 검증도 필요
//        assertEquals("존재하지 않는 글이지롱.", e.getMessage());
//        assertEquals("존재하지 않는 글입니다.", e.getMessage());

        // ♻️ 사용자 정의 예외로 테스트
        assertThrows(PostNotFound.class, () -> postService.get(post.getId() + 1L));
        assertThrows(IllegalArgumentException.class, () -> postService.get(post.getId() + 1L));
    }

    // 전체 조회 테스트 -> 우선 글을 먼저 저장해주는 작업이 필요
    @Test
    @DisplayName("글 전체 조회")
    void test4(){
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
        // List<PostResponse> postResponses = postService.getAll();

        // then
        // assertEquals(2, postResponses.size());
    }

    // 글 전체 조회 테스트 - 페이징 처리
    @Test
    @DisplayName("글 전체 조회 - 페이징 처리, 1페이지 조회")
    void test5(){
        // 페이징 처리를 위해서는 ✅ SQL -> SELECT, LIMIT, OFFSET 모두 알아야 함!
        // given
        List<Post> requestPosts = IntStream.range(1, 31)    // for(int i=0;i<30;i++)와 같은 기능
                .mapToObj(i -> {                        // mapToObj : Entity로 변환
                    return Post.builder()
                            .title("의연 제목 - " + i)
                            .content("반포자이 - " + i)
                            .build();
                })
                .collect(Collectors.toList());

        postRepository.saveAll(requestPosts);

        Pageable pageable = PageRequest.of(0, 5, Sort.by(Direction.DESC, "id"));


        // when - getAll()에 페이지 번호를 입력하면 해당하는 페이지 게시글만 가져오도록 수정 필요
        List<PostResponse> postResponses = postService.getAll(pageable);

        // then
        assertAll(
                () -> assertEquals(5, postResponses.size()),
                () -> assertEquals("의연 제목 - 30", postResponses.get(0).getTitle()),
                () -> assertEquals("반포자이 - 30", postResponses.get(0).getContent())
        );
    }

    // 글 조회 테스트 - QueryDSL
    @Test
    @DisplayName("글 전체 조회 - QueryDSL 사용, 1페이지 조회")
    void test6(){
        // given
        List<Post> posts = IntStream.range(1, 21)
                .mapToObj(i ->
                    Post.builder()
                            .title("의연 제목 - " + i)
                            .content("의연 내용 - " + i)
                            .build())
                .toList();

        postRepository.saveAll(posts);

        Pageable pageable = PageRequest.of(0, 5);

        // when
        List<PostResponse> postResponses = postService.getAll(pageable);

        // then
        assertAll(
                () -> assertEquals(5L, postResponses.size()),
                () -> assertEquals("의연 제목 - 20", postResponses.get(0).getTitle())
        );
    }

    // 글 조회 테스트 - PostSearch
    @Test
    @DisplayName("글 전체 조회 - QueryDSL 사용, PostSearch 사용")
    void test7(){
        // given
        List<Post> posts = IntStream.range(1, 21)
                .mapToObj(i ->
                        Post.builder()
                                .title("의연 제목 - " + i)
                                .content("의연 내용 - " + i)
                                .build())
                .toList();

        postRepository.saveAll(posts);

        PostSearch postSearch = PostSearch.builder()
                .page(1)
                .pageSize(10)
                .build();

        // when
        List<PostResponse> postResponses = postService.getAll(postSearch);

        // then
        assertAll(
                () -> assertEquals(10L, postResponses.size()),
                () -> assertEquals("의연 제목 - 20", postResponses.get(0).getTitle())
        );
    }

    // 업데이트 테스트 - 제목 수정 테스트
    @Test
    @DisplayName("글 제목 수정")
    void test8(){
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
    void test9(){
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

    @Test
    @DisplayName("글 내용 수정 - 존재하지 않는 글 수정")
    void test11(){
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

        // expected
        assertThrows(PostNotFound.class,
                () -> postService.edit(post.getId() + 1L, postEdit));
    }
    
    // 삭제 테스트
    @Test
    @DisplayName("게시글 삭제")
    void test12(){
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
    
    @Test
    @DisplayName("게시글 삭제 - 존재하지 않는 글 삭제")
    void test13(){
        // given
        Post post = Post.builder()
                .title("의연 삭제 제목")
                .content("의연 삭제 내용")
                .build();
        postRepository.save(post);

        // expected
        assertThrows(PostNotFound.class,
                () -> postService.delete(post.getId() + 1L));
    }
}