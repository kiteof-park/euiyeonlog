package com.euiyeonlog.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
// 수정할 수 있는 필드들에 대해서만 정의
public class PostEditor {
    private final String title;
    private final String content;

    @Builder
    public PostEditor(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
