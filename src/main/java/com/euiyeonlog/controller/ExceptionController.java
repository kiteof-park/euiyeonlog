package com.euiyeonlog.controller;

import com.euiyeonlog.exception.EuiyeonlogException;
import com.euiyeonlog.response.ErrorResposne;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


@Slf4j
@ControllerAdvice
public class ExceptionController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody   // ControllerAdvice에 ReponseBody가 없으면 메서드 반환값인 Map<String, String>이 View이름으로 해석
    public ErrorResposne invalidRequestHandler(MethodArgumentNotValidException e){
        ErrorResposne response = ErrorResposne.builder()
                .code("400")
                .message("잘못된 요청입니다.")
                .build();

        for(FieldError fieldError : e.getFieldErrors()){
            response.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return response;

        // ✅ Excepotion에 대한 응답을 클래스로 정의해서 사용
//        // log.error("exceptionHandler error", e);
//        // 받은 Exception을 토대로 에러 처리를해서 JSON형태로 응답을 만들어주는 작업이 필요
//        FieldError fieldError = e.getFieldError();
//        String field = fieldError.getField();
//        String message = fieldError.getDefaultMessage();
//
//        // 어떻게 JSON으로 넘겨줄까?
//        Map<String, String> response = new HashMap<>();
//        response.put(field, message);
//        return response;
    }

    @ResponseBody
    @ExceptionHandler(EuiyeonlogException.class)
    public ResponseEntity<ErrorResposne> euiyeonlogException(EuiyeonlogException e) {

        int statusCode = e.getStatusCode();

        ErrorResposne errorResposne = ErrorResposne.builder()
                .code(String.valueOf(statusCode))
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(statusCode).body(errorResposne);
    }

//    @ResponseBody
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ExceptionHandler(EuiyeonlogException.class)
//    public ErrorResposne invalidRequestHandler(InvalidRequest e) {
//
//        ErrorResposne errorResposne = ErrorResposne.builder()
//                .code("400")
//                .message(e.getMessage())
//                .build();
//
//        return errorResposne;
//    }
}
