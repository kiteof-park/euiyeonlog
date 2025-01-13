package com.euiyeonlog.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


// PostEdit이랑 PostCreate랑 퉁칠까 .. ?
// ⚠️ 역할, 기능이 다르면 코드가 비슷하더라도 명확하게 분리하는게 중요
@Getter
@ToString
public class PostEdit {
    @NotBlank(message = "title을 입력하세요")
    private String title;

    @NotBlank(message = "content를 입력하세요")
    private String content;

    @Builder
    public PostEdit(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
