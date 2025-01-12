package com.euiyeonlog.service;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.respository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    //  📌 @Autowired를 통한 필드 주입 지양❌ -> 생성자 주입✅
    //  📌 근데 생성자 주입 대신 Lombok의 @RequiredArgsConstructor를 이용
    private final PostRepository postRepository;

    // 글을 저장하는 메서드
    public void write(PostCreate postCreate){
        
        //  📌 레포지토리에 저장되는 것은 엔티티, DTO로 저장❌ - DTO를 엔티티로 변환 필요❗❗
//        postRepository.save(postCreate);

        //  아래와 같은 방법은 지양❌ - 필드에 바로 꽂아넣는 형식
        //  📌 Post Entity의 AccessLevel, 필드의 접근 제어자를 public으로 변경해야 함
//        Post post = new Post();
//        post.title = postCreate.getTitle();
//        post.content = postCreate.getTitle();
//        postRepository.save(post);

        //  📌 Post Entity의 필드 접근 제어자는 private을 유지하고, 생성자를 만듦
        // Post post = new Post(postCreate.getTitle(), postCreate.getContent());

        // 빌더를 이용해 변경 -> Post 엔티티에 @Buidler 추가
        Post post = Post.builder()
                .title(postCreate.getTitle())
                .content(postCreate.getContent())
                .build();
        postRepository.save(post);
    }
}
