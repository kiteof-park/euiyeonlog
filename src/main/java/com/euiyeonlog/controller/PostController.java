package com.euiyeonlog.controller;

import com.euiyeonlog.domain.Post;
import com.euiyeonlog.request.PostCreate;
import com.euiyeonlog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

// ✅ 데이터 기반  API응답 생성을 위해 RestController 사용
// ✅ HttpMessageConverter를 이용해 JSON형태로 응답
// ✅ RestController = @ResponseBody + @Controller
@RestController
@RequiredArgsConstructor
@Slf4j
public class PostController {
    private final PostService postService;
    // SSR -> JSP, Thymeleaf, Mustache, Freemarker
    // SPA ->
        // Vue -> Vue + SSR = nuxt.js
        // React -> React + SSR = next.js
    
    // SSR -> 서버에서 렌더링을 해서 데이터를 내려줌(Html Rendering)
    // SPA -> Vue -> Javascript에서 화면을 만들어주고, 서버와의 통신은 API로만 통신, JSON형태로 응답처리

    // @RequestMapping(method = RequestMethod.GET, path = "/posts" 와 같음
    @GetMapping("/posts")
    public String get(){
        return "Hello World";
    }
    
    // ✅ 컨트롤러 작성 후 테스트 작성

    // ✅ HTTP Method : GET, POST, PUT, PATCH, HEAD, OPTIONS, TRACE, CONNECT

    // 📌 글 등록1 - @ControllerAdvice, @ExceptionHandler
//    @PostMapping("/posts")
//    public Map<String, String> post(@RequestBody @Valid PostCreate postCreate){
//         // 레포지토리에 바로 저장 vs 서비스 레이어를 통해 레포지토리를 호출해 저장 ? 서비스 레이어 이용✅
//        postService.write(postCreate);
//        return Map.of();
//    }

    // 📌 글 등록2 - 응답 값 변경
    @PostMapping("/posts")
    public void post(@RequestBody @Valid PostCreate request){
        // Case 1. 저장한 데이터 Entity -> Response로 응답하기
        // Case 2. 저장한 데이터의 Pk id -> Response로 응답하기
                // 클라이언트에서는 응답받은 id를 조회 API를 통해 데이터를 응답 받음?
        // Case 3. 응답 필요 없음
                // 클라이언트에서 모든 글 데이터 Context를 잘 관리함
        postService.write(request);
    }

    @GetMapping("/posts/{postId}")
    public Post get(@PathVariable(name = "postId") Long id){
        Post post = postService.get(id);
        return post;
    }

    // 📌 글 등록1 - POST Method
    // x-www.form-urlencoded 형태의 데이터를 서버로 요청(@RequestParam을 사용)
//    @PostMapping("/posts")
//     public String post(@RequestParam String title, @RequestParam String content){
////        System.out.println("title = " + title);
////        System.out.println("content = " + content);
//
//        //  ✅ println()대신 Lombok의 @Slf4j를 사용
//        log.info("title={}, content={]", title, content);
//        return "Hello World";
//    }

    // 📌 글 등록2 - POST Method
//    @PostMapping("/posts")
//    public String post(@RequestParam Map<String, String> params){
//        log.info("params={}", params);
//        String title = params.get("title");
//        String content = params.get("content");
//        return "Hello World";
//    }

    // 📌 글 등록3 - POST Method,
    // ✅ 요청 데이터를 받을 클래스 PostCreate를 생성 - DTO역할
    // 🔎 @ModelAttribute는 요청 데이터를 모델 객체에 자동 바인딩, 뷰에 전달할 데이터를 추가
//    @PostMapping("/posts")
//    public String post(@ModelAttribute PostCreate params){  // ✅ PostCreate에 @Setter 추가해야 null❌
//        log.info("params={}", params.toString());
//        return "Hello World";
//    }

    // 📌 글 등록4 - POST Method
//    @PostMapping("/posts")
//    public Map<String, String> post(@RequestBody @Valid PostCreate params, BindingResult result){
//        // ✅데이터 검증 필요 -> PostCreate의 @NotBlank 어노테이션으로 해결!!
////        var title = params.getTitle();
////        if(title == null || title.equals("")){
////            throw new Exception("title값이 없서용!");
////        }
////        var content = params.getContent();
////        if(content == null || content.equals("")){
////            throw new Exception("content값이 없서용!");
////        }
//
//        // ✅데이터 검증 실패 결과를 클라이언트에게 알리고 싶다면? -> BindingResult
//        if(result.hasErrors()){
//            List<FieldError> fieldError = result.getFieldErrors();
//            FieldError firstFieldError = fieldError.get(0);
//            String fieldName = firstFieldError.getField();
//            String errorMessage = firstFieldError.getDefaultMessage();
//
//            Map<String, String> error = new HashMap<>();
//            error.put(fieldName, errorMessage);
//            return error;
//        }
//
//        log.info("params={}", params.toString());
//        return Map.of();    // 빈 Map을 생성 - immutable Map
////        return "Hello World";
//    }



    // 📌 글 등록5 - POST Method
//    @PostMapping("/posts")
//    public String post(@RequestBody PostRequest request){
//        log.info("title={}, content={}", request.getTitle(), request.getContent());
//        return "안뇽💖";
//    }
}

// 📌 글 등록4를 위한 클래스 - DTO 역할
// 📌 @RequestBody는 JSON 데이터를 DTO 객체에 매핑
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//class PostRequest{
//    private String title;
//    private String content;
//}

/* 📌 @RequestParam과 @RequestBody
* 글 등록1을 작성하고 Postman을 활용해 테스트를 해보려고 했는데 "405 Method Not Allowed" 에러 발생
* 원인은 @RequestParam과 @RequestBody에 있었다 ❗❗
*
*
* ✅ @RequestParam
* HTTP 요청의 파라미터값(쿼리 파라미터, 폼 데이터)를 바인딩할 때 사용
* URL 쿼리 문자열 또는 폼 데이터에서 값을 추출
* 주로 GET요청이나, x-www-form-urlencoded타입의 POST 요청에 사용
* @ReqeustParam은 JSON 데이터 처리에 적합하지 않음❌
*
* ✅ @RequestBody
* HTTP 요청의 본문(body) 데이터를 객체로 매핑할 때 사용 - 요청 본문 데이터를 자바 객체로 매핑
* 주로 POST, PUT 요청에서 JSON, XML 데이터를 처리
* */

