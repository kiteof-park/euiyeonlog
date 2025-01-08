package com.euiyeonlog.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// 📌 PostCreate는 DTO역핳
// ✅ 요청으로 받은 데이터를 PostCreate로 받고, 그 값을 저장하기 위해 @Setter 추가
@Setter
@Getter
@ToString
public class PostCreate {

    /* 📌
    * message는 controller에서 @Valid에 의해 검증하고, 그 결과를 BindingResult에 저장할 때 사용
    * 만약 에러가 발생하면 message로 지정한 값을 저장함
    * */
    @NotBlank(message = "title을 입력해주세요")
    private String title;

    @NotBlank(message = "content를 입력해주세요")
    private String content;

    // ✅ toString() 오버라이딩 대신 Lombok의 @ToString을 사용
//    @Override
//    public String toString() {
//        return "PostCreate{" +
//                "title='" + title + '\'' +
//                ", content='" + content + '\'' +
//                '}';
//    }
}
