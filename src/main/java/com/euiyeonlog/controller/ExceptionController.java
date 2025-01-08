package com.euiyeonlog.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ExceptionController {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> invalidRequestHandler(MethodArgumentNotValidException e){
        // log.error("exceptionHandler error", e);
        // 받은 Exception을 토대로 에러 처리를해서 JSON형태로 응답을 만들어주는 작업이 필요
        FieldError fieldError = e.getFieldError();
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();

        // 어떻게 JSON으로 넘겨줄까?
        Map<String, String> response = new HashMap<>();
        response.put(field, message);
        return response;
    }
}
