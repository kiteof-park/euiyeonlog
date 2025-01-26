package com.euiyeonlog.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

// Exception이 발생했을 때 반환하는 내용을 ErrorResponse 클래스로 정의

/* 📌이런 응답의 문제점은 사용자에게 어떤 값이 잘못됐는지 응답❌
* {
*       "code" : "400",
*       "message" : "잘못된 요청입니다",
* }
* */

/* 📌이런 응답의 문제점은 사용자에게 어떤 값이 잘못됐는지 응답❌
 * {
 *       "code" : "400",
 *       "message" : "잘못된 요청입니다",
 *       "validation" : {
 *              "title" : "값을 입력해주세요"
 *       }
 * }
 * */

@Getter
@Builder
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResposne {
    private final String code;
    private final String message;
    private final Map<String, String> validation = new HashMap<>();

    public void addValidation(String fieldName, String errorMessage){
        this.validation.put(fieldName, errorMessage);
    }
}
