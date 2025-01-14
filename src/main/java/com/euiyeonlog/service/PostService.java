package com.euiyeonlog.service;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.domain.PostEditor;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.request.PostEdit;
import com.euiyeonlog.response.PostResponse;
import com.euiyeonlog.respository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

    // 글 조회하는 메서드
    public PostResponse get(Long id){
        // 📌 Optional 데이터는 가져와서 즉시 꺼내는걸 추천
//        Optional<Post> postOptional =postRepository.findById(id);
//        if(postOptional.isPresent()){
//            Post post = postOptional.get();
//        }

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 📌 조회해온 엔티티를 변환 -> 이 변환 작업을 서비스 레이어에서 하는게 맞을까?
        /* 서비스 레이어를 웹 서비스, 서비스로 나눠서 생각해보기
        *  Post Controller -> WebPostSerivce -> Repostiory
        *                  -> PostService
        *  ✅ WebPostSerivce : Response를 위해서 행위를 하는 서비스 호출을 담당
        *  ✅ PostService : 외부 연동을 하는 서비스와 통신하는 서비스를 담당
        * */
        PostResponse response = PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
        return response;
    }

//    public Post getRss(Long id){
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
//        return post;
//    }

    // 게시글 수정 - 수정해야 할 게시글 식별번호(pk)와 수정할 내용(PostEdit) 필요
    @Transactional
    public void edit(Long id, PostEdit postEdit){
        // id를 통해 게시글 하나 가져오기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 1. 제목, 내용 변경 -> ❌엔티티에 @Setter달아서 수정하기? 지양!!❌
//        post.setTitle(postEdit.getTitle());
//        post.setContent(postEdit.getContent());
        
        // 2. 엔티티에 @Setter를 제거하고, 제목과 내용을 변경하는 메서드 추가
        // post.change(postEdit.getTitle(), postEdit.getContent());

        // 📍 PostEdtior를 사용한 게시글 수정(1)
        //  3. PostEditor와 빌드되지 않은 빌더 클래스를 이용
        PostEditor.PostEditorBuilder postEditorBuilder = post.toEditor();

        // 이거 뭐지
//        if (postEdit.getTitle() != null){
//            postEditorBuilder.title(postEdit.getTitle());
//        }
//        if (postEdit.getContent() != null){
//            postEditorBuilder.content(postEdit.getContent());
//        }
//        post.edit(postEditorBuilder.build());

        // 📍 PostEdtior를 사용한 게시글 수정(2)
        PostEditor postEditor = postEditorBuilder.title(postEdit.getTitle())
                .content(postEdit.getContent())
                .build();// 값을 변경하고 빌드 (값을 픽스시킴)

        post.edit(postEditor);

        // 📍 PostEdtior를 사용하지 않고 게시글 수정
        // post.edit(postEdit.getTitle(), postEdit.getContent());

        // 레포지토리 save -> @Transactioonal로 대체
        // postRepository.save(post);
    }

    public void delete(Long id){
        // id로 게시글 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        postRepository.deleteById(id);
        // 또는
        // postRepository.delete(post);
    }
}
