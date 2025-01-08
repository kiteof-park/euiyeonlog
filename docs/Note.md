> ### 뭔가 3번 이상의 반복작업을 한다면, 내가 잘못하고 있는건 아닌지 의심해보기👀 

# 8강 POST 데이터 콘텐츠 타입

## x-www-form-urlencoded
`x-www-form-urlencoded` 데이터 형식의 경우  
데이터를 넘겨줄 때   
`?userId=euiyeon0519&userName=박의연&title=의연&content=최고`  
이런식으로 데이터를 완전 풀어서 표현을 해야하므로 한계가 있음


## JSON
그에 비해 `JSON`은  
{  
"title" : "xxx",  
    "content" : "xxx",  
    "user" : {  
        "id" : "xxx",  
        "name" : "xxx" }    
}  
🪄`user`의 경우 새로운 객체를 만들면 됨  
이런식으로 데이터를 온전하게 표현을 할 수 있기 때문에 큰 장점이 있음  
클래스로 표현하기에도 용이  
---
# 9강 데이터 검증1

## 데이터를 검증하는 이유
1. 값을 보내는 클라이언트 개발자의 실수 _- Human Error_
2. 클라이언트 버그로 값이 누락될 수 있음
3. 외부에서 데이터를 조작할 수 있음
4. DB에 값을 저장할 때 의도치 않은 오류가 발생할 수 있음
5. 서버 개발자의 편안함을 위해💖

## 데이터 검증에 대해 생각해볼거리
```java
    @PostMapping("/posts")
    public String post(@RequestBody PostCreate params) throws Exception{
        // ✅데이터 검증 필요
        var title = params.getTitle();
        if(title == null || title.equals("")){
            throw new Exception("title값이 없서용!");
        }
        var content = params.getContent();
        if(content == null || content.equals("")){
            throw new Exception("content값이 없서용!");
        }
        log.info("params={}", params.toString());
        return "Hello World";
    }
```
- 지금은 `PostCreate`에 필드가 `title`, `content`밖에 없지만,  
수십 개의 필드가 있다면 ..? 위와 같은 형태의 검증은 비효율❌(`if`문 떡칠)
  - 빡세다(노가다임)
  - 누락 위험
  - ✅ 생각보다 검증해야될 게 많다(꼼꼼하지 않을 수 있음)
    - `{"title" : ""}`
    - `{"title" : "　　　　　　　　　　"}`
    - `{"title" : "...........수십억글자"}`
  - 간지가 안난다(개발자스럽지 않다)

## Spring Boot Starter Validator 라이브러리
- `@NotBlank`과 같은 데이터 검증을 위한 어노테이션이 있음
- `org.springframework.boot:spring-boot-starter-validation`
- 정말 좋자나 ...?

## @NotBlank의 검증과정
📌`PostCreate.java`  
- `@NotBlank` 추가
```java
@Setter
@Getter
@ToString
public class PostCreate {
    @NotBlank
    private String title;
    
    @NotBlank
    private String content;
}
```
📌`PostController.java`  
- `@Valid` 추가  
- 요청값에 대한 검증을 자동으로 실행
- Request DTO에 `@Valid`를 추가하는 것

```java
    @PostMapping("/posts")
    public String post(@RequestBody @Valid PostCreate params) throws Exception{
        ...
    }
```
### @NotBlank의 검증 대상
- null → 허용 ❌
- 빈 문자열("") → 허용 ❌
- 공백만 포함(" ") → 허용 ❌

### @NotNull vs @NotBlank
#### `@NotNull`
- 값이 `null` 인지 여부만 검사
- 빈 문자열(`""`)이나 공백 문자열(`"  "`)을 허용
#### `@NotBlank`
- `null`, 빈 문자열(`""`), 공백 문자열(`"   "`)을 모두 허용하지 않음
- 문자열(String)에서만 사용 가능
- 문자열 검사에 특화

## @Valid의 검증 과정
- `@Valid`는 **요청 데이터가 컨트롤러에 도달하기 전에** Spring의 검증 과정을 먼저 수행  
- 유효성 검증에 실패하면, 컨트롤러 메서드가 호출되기 전에 예외(Exception)발생
  - 이 예외는 `MethodArgumentNotValidException` 타입
  - Spring에서 자동으로 처리하지 않으면, 기본 에러 페이지나 400 상태코드만 반환

## 잘못된 값이 넘어왔을 때 클라이언트에게 알릴 수 있나?
- `@Valid`는 컨트롤러 실행 전에 검증을 수행
- **검증 실패 시 예외가 발생하면 Spring이 자동으로 `400 Bad Request` 응답을 반환**
- 즉, 클라이언트에게 오류가 발생했다는 사실을 알릴 수 있지만,  
기본적으로 제공되는 오류 메시지가 비직관적이고 사용자 친화적이지 않음
- `BindingResult`를 이용해서 해결

##  BindngResult
- `{"title" : "타이틀값이 없습니다."}`라는 응답을 주고 싶은 경우 
  - 데이터 검증에 실패해도 컨트롤러 메서드에 도달하기 위한 방법?
- BindingResult에는 에러와 관련된 내용이 모두 담김(`errors`)
  - `field = "title"`
  - `rejectedValue = ""`
  - `defaultMessage = "must not be blank"`

```java
    @PostMapping("/posts")
    public Map<String, String> post(@RequestBody @Valid PostCreate params, BindingResult result){
        // ✅데이터 검증 실패 결과를 클라이언트에게 알리고 싶다면? -> BindingResult
        if(result.hasErrors()){
            List<FieldError> fieldError = result.getFieldErrors();
            FieldError firstFieldError = fieldError.get(0);
            String fieldName = firstFieldError.getField();
            String errorMessage = firstFieldError.getDefaultMessage();

            Map<String, String> error = new HashMap<>();
            error.put(fieldName, errorMessage);
            return error;
        }

        log.info("params={}", params.toString());
        return Map.of();    // 빈 Map을 생성 - immutable Map
    }
``` 


## BindingResult란?
- `BindingResult`는 SpringFramework에서 유효성 검증 결과를 담는 객체
- 폼 데이터, JSON 데이터를 DTO 객체로 매핑하거나 유효성 검증을 수행할 때 사용

### BindingReuslt의 역할 및 기능
1. **검증 결과 및 저장 :** 입력 데이터가 검증 조건을 만족하는지 검사한 결과 저장
2. **오류 정보 제공 :** 어떤 필드에서 오류가 발생했는지, 어떤 메세지를 반환할지 관리
3. **검증 실패 시 예외 방지 :**
   - `@Valid`만 사용할 경우, 검증 실패시 예외(`MethodArgumnetNotValidException`)발생
   - `BindingResult`를 함께 사용하면, 예외가 발생하지 않고 코드 흐름을 게속 진행
### BindingResult의 동작 원리
1. 요청 데이터가 DTO 객체에 매핑
2. `@Valid` 어노테이션이 DTO 유효성 검사를 수행
3. 검증 결과는 `BindingResult`객체에 저장
4. 컨트롤러 내부에서 검증 결과를 확인하고, 오류가 있으면 사용자 친화적 응답 반환
### 언제 BindingResult를 사용?
1. **간단한 검증만 필요할 때**
   - 검증 실패 시 단순 오류 메시지만 반환하면 되는 경우
   - 사용자 등록, 로그인 요청
2. **복잡한 검증 로직을 처리할 때**
   - 필드 오류 외에 비지니스 검증 로직(중복 확인, 조건 검증)이 필요한 경우
   - 검증 실패 여부에 따라 다른 처리 로직을 실행할 때 유용
3. **예외 핸들러를 도입하지 않을 때**
   - 프로젝트 규모가 작고, 검증 오류 처리를 개별 컨트롤러에서 처리할 때 사용
   - 규모가 커지면 글로벌 예외 핸들러가 더 유용

```java
    @PostMapping("/posts")
    public Map<String, String> post(@RequestBody @Valid PostCreate params, BindingResult result){
        // ✅데이터 검증 필요 -> PostCreate의 @NotBlank로 해결 !!

        // ✅데이터 검증 실패 결과를 클라이언트에게 알리고 싶다면? -> BindingResult
        if(result.hasErrors()){
            List<FieldError> fieldError = result.getFieldErrors();
            FieldError firstFieldError = fieldError.get(0);
            String fieldName = firstFieldError.getField();
            String errorMessage = firstFieldError.getDefaultMessage();

            Map<String, String> error = new HashMap<>();
            error.put(fieldName, errorMessage);
            return error;
        }

        log.info("params={}", params.toString());
        return Map.of();
    }
```
```java
@Setter
@Getter
@ToString
public class PostCreate {
    @NotBlank(message = "title을 입력해주세요")
    private String title;

    @NotBlank(message = "content를 입력해주세요")
    private String content;
}
```

### jsonPath에 대한 검증 방법 -> 알아보기

---

# 10강 데이터 검증2
- Spring 자체적으로 400 오류를 발생하는 방법 대신,  
BindingResult`를 통해 직접 error를 컨트롤해서 클라이언트에게 에러 메세지 전달
```java
    // 📌 글 등록4 - POST Method
    @PostMapping("/posts")
    public Map<String, String> post(@RequestBody @Valid PostCreate params, BindingResult result){
    
        if(result.hasErrors()){
            List<FieldError> fieldError = result.getFieldErrors();
            FieldError firstFieldError = fieldError.get(0);
            String fieldName = firstFieldError.getField();
            String errorMessage = firstFieldError.getDefaultMessage();

            Map<String, String> error = new HashMap<>();
            error.put(fieldName, errorMessage);
            return error;
        }

        log.info("params={}", params.toString());
        return Map.of(); 
    }
```
### 위와 같은 방식의 문제점
1. 메서드마다 검증 코드가 필요
   - 컨트롤러의 메서드가 수십개가 된다면 ...?
   - 반복적인 작업이 발생
   - 개발자가 까먹을 수 있음
   - 검증 부분에서 버그가 발생할 여지가 높다
2. 응답 값에 `HashMap` 타입을 사용했는데, 응답 값은 맵이 아니라 응답에 맞는 클래스 정의가 좋음
3. 여러 개의 에러 처리가 힘듦
   - 위에 코드는 첫 번째 에러만 처리(title에 대한 에러)

### 결론 _- `@ControllerAdvice`, `@ExceptionHandler` 를 통해 해결_
- 개별 컨트롤러가 아닌, 모든 컨트롤러에 대한 검증 에러를 캐치
- `@ControllerAdvice`와 `@ExceptionHandler`로 에러를 캐치하기 위해서는.
**`BindingResult`**를 제거

📌`PostController.java`
```java
   @PostMapping("/posts")
    public Map<String, String> post(@RequestBody @Valid PostCreate params){
        return Map.of();
    }
```

📌`ExceptionController.java`
```java
@ControllerAdvice
public class ExceptionController {
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(){
        System.out.println("우하하");
    }
}
```
