package com.euiyeonlog.domain;

import lombok.Builder;
import lombok.Generated;
import lombok.Getter;

@Getter
// 수정할 수 있는 필드들에 대해서만 정의
public class PostEditor {
    // final제거
//    private final String title;
//    private final String content;
    private final String title;
    private final String content;

    // @Builder 제거하고 수동으로 작성 -> build 패키지에서 뽀려오기
//    @Builder
    public PostEditor(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public static PostEditor.PostEditorBuilder builder() {
        return new PostEditorBuilder();
    }

    public static class PostEditorBuilder {

        private String title;

        private String content;


        PostEditorBuilder() {
        }

        public PostEditorBuilder title(final String title) {
            if(title != null){
                this.title = title;
            }
            return this;
        }

        public PostEditorBuilder content(final String content) {
            if(content != null){
                this.content = content;
            }
            return this;
        }

        public PostEditor build() {
            return new PostEditor(this.title, this.content);
        }

        public String toString() {
            return "PostEditor.PostEditorBuilder(title=" + this.title + ", content=" + this.content + ")";
        }
    }
}
