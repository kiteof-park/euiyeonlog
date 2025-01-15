package com.euiyeonlog.response;

import com.euiyeonlog.domain.Post;
import lombok.Builder;
import lombok.Getter;

// 서비스 정책에 맞는 클래스
@Getter
public class PostResponse {
    private Long id;
    private String title;
    private String content;

    @Builder
    public PostResponse(Long id, String title, String content) {
        this.id = id;
        // title이 10글자 이하일때 발생하는 에러를 방지하기 위해 Math.min()사용
        this.title = title.substring(0, Math.min(title.length(), 10));
        this.content = content;
    }

    // 생성자 오버로딩 - 반복되는 빌더 코드를 줄이기 위함
    public PostResponse(Post post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
    }
}
