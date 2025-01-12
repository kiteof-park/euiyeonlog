package com.euiyeonlog.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 📌 PostCreate는 DTO역핳
// ✅ 요청으로 받은 데이터를 PostCreate로 받고, 그 값을 저장하기 위해 @Setter 추가
@Setter
@Getter
@ToString
// @AllArgsConstructor
// @Builder
public class PostCreate {

    /* 📌
    * message는 controller에서 @Valid에 의해 검증하고, 그 결과를 BindingResult에 저장할 때 사용
    * 만약 에러가 발생하면 message로 지정한 값을 저장함
    * */
    @NotBlank(message = "title을 입력해주세요")
    private String title;

    @NotBlank(message = "content를 입력해주세요")
    private String content;

    // ✅ 생성자는 Lombok의 @AllArgsConstructor를 사용
    // ✅ 근데 또 생성자 대신 @Builder 사용 권장
    // @Builder는 클래스 위에 vs 생성자 위에 -> 호돌씨는 생성자 위에 다는걸 권장 -> 왜? ... ? ? ?
    // PostCreate는 모든 필드를 초기화하므로 클래스에 @Builder를 붙여도 상관없음
    @Builder
    public PostCreate(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // ✅ toString() 오버라이딩 대신 Lombok의 @ToString을 사용
//    @Override
//    public String toString() {
//        return "PostCreate{" +
//                "title='" + title + '\'' +
//                ", content='" + content + '\'' +
//                '}';
//    }

    // 빌더의 장점
    // 1. 가독성
    // 2. 값 생성에 대한 유연함
    // 3. 필요한 값만 받을 수 있다.
    // ✅ 4. 객체의 불변성 - 제일 중요!
}
