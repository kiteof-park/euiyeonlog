package com.euiyeonlog.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
// @Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // @Lob : 자바에서는 String, DB에서는 Long text형태로 되도록 함
    @Lob
    private String content;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
    // ❌엔티티에 getter메서드를 만들때는 절대 서비스에 맞는 정책을 넣지 말것❌
//    public String getTitle(){
//        return title.substring(0, 10);
//    }

    // 메서드 매개변수의 순서가 바껴서 들어온다면? title에 content저장, content에 title저장
//    public void change(String title, String content){
//        this.title = title;
//        this.content = content;
//    }

    // 📍  PostEditor를 이용한 게시글 수정(1)
    // 📌 빌드하지 않은 빌더 클래스 자체를 반환
    // 📌 엔티티의 값을 그대로 복사해서 빌더 클래스로 반환
    public PostEditor.PostEditorBuilder toEditor(){
        return PostEditor.builder()
                .title(title)
                .content(content);
    }

    // 📍  PostEditor를 이용한 게시글 수정(2)
    // 📌 값이 픽스된 PostEditor가 넘어옴
    // 1. PostEditor 딱 한개만 인자로 받는 메서드로 개선 가능
    // 2. PostEditor내에 수정 가능한 필드만 좁혀서 선언 가능
    public void edit(PostEditor postEditor){
        this.title = postEditor.getTitle();
        this.content = postEditor.getContent();
    }

    // 📍 2. PostEditor를 사용하지 않고 업데이트 기능 구현
//    public void edit(String title, String content){
//        this.title = title;
//        this.content = content;
//    }
}
