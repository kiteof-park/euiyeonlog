package com.euiyeonlog.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
}
