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

### BindingResult의 역할 및 기능
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
`BindingResult`를 통해 직접 error를 컨트롤해서 클라이언트에게 에러 메세지 전달
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
**1. 메서드마다 검증 코드가 필요**
   - 컨트롤러의 메서드가 수십개가 된다면 ...?
   - 반복적인 작업이 발생
   - 개발자가 까먹을 수 있음
   - 검증 부분에서 버그가 발생할 여지가 높다

**2. 응답 값에 `HashMap` 타입을 사용했는데,  
응답 값은 `Map`이 아니라 응답에 맞는 클래스 정의가 좋음**

**3. 여러 개의 에러 처리가 힘듦**
   - 위에 코드는 첫 번째 에러만 처리(`fieldErrors.get(0)`)  

### 결론 _- `@ControllerAdvice`, `@ExceptionHandler` 를 통해 해결_
- 개별 컨트롤러가 아닌, 모든 컨트롤러에 대한 검증 에러를 캐치
- `@ControllerAdvice`와 `@ExceptionHandler`로 에러를 캐치하기 위해서는
`BindingResult`를 제거

📌`PostController.java` 
```java
   @PostMapping("/posts")
    public Map<String, String> post(@RequestBody @Valid PostCreate params){
        return Map.of();
    }
```

📌`ExceptionController.java`
```java
@Slf4j
@ControllerAdvice
public class ExceptionController {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> invalidRequestHandler(MethodArgumentNotValidException e){
        
        // 받은 Exception을 토대로 에러를 처리해서 JSON형태로 응답을 만들어주는 작업이 필요
        FieldError fieldError = e.getFieldError();
        String field = fieldError.getField();
        String message = fieldError.getDefaultMessage();

        // 어떻게 JSON으로 넘겨줄까?
        Map<String, String> response = new HashMap<>();
        response.put(field, message);
        return response;
    }
}
```
## @ControllerAdvice
- 전역 예외 처리(Global Exception Handler), 데이터 바인딩, 모델 속성 추가 등을 제공하는  
전역 컨트롤러 보조 기능을 제공
- **특정 컨트롤러에 국한되지 않고, 애플리케이션 전체의 컨트롤러에서 발생하는 예외를  
한 곳에서 일괄적으로 처리 가능**

### 1. 전역 예외 처리
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 특정 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return errors;
    }
}
```
- `@ControllerAdvice`: 애플리케이션 전역의 컨트롤러 예외를 처리.
- `@ExceptionHandler`: 처리할 예외 타입을 지정하여 해당 예외 발생 시 호출되는 메서드 정의.
- `@ResponseBody`: JSON 형식으로 응답을 반환하도록 설정.
- `@ResponseStatus`: HTTP 상태 코드를 명시 (400 Bad Request).

### 2. 전역 데이터 추가
```java
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("appName")
    public String appName() {
        return "My Application";
    }
}
```
- 모든 View에서 사용할 수 있는 전역 변수 appName 추가
- 예제에서는 "My Application"이라는 값을 모델에 추가
- 모든 뷰에서 이 변수를 사용 가능

## @ExceptionHandler
- 특정 컨트롤러 또는 전역 컨트롤러(@ControllerAdvice) 내에서 발생하는 예외를 처리
- 예외가 발생했을 때 예외 처리 로직을 직접 구현

## @ControllerAdvice vs @RestContollerAdvice
| 어노테이션              | @ControllerAdvice                | @RestControllerAdvice             |
|--------------------|----------------------------------|-----------------------------------|
| 기본 동작              | HTML 뷰 또는 JSON 데이터 반환 가능         | 기본적으로 JSON데이터(응답 본문)을 반환          |
| `@ResponseBody` 필요여부 | JSON 응답 시 `@ResponseBody`를 메서드에 추가 | 자동으로 `@ResponseBody` 적용           |
| 주요 사용 사례           | HTML 뷰와 API응답을 혼용하는 프로젝트         | REST API 전용 프로젝트                  |
| 내부 구현              | `@ControllerAdvice`                | `@ControllerAdvice` + `@ResponseBody` |

---

##  10강 정리
- 컨트롤러에서 `@RequestBody`를 통해 요청 데이터를 `PostCreate`(객체)에 매핑
- `PostCreate`에는 검증 어노테이션을 추가(`@NotBlank`)하고, 컨트롤러 메서드에는 `@Valid`를 추가해 입력 데이터를 검증
- 입력 데이터 검증 과정에서 에러가 발생하면 `@ControllerAdvice`와 `@ExceptionHanlder`를 통해 에러를 처리
- 에러에 대한 정보를 반환하기 위해 `ErrorResponse`를 작성  

🪄 클라이언트가 JSON형식의 HTTP 요청을 전송하면 `@RequestBody`는 HTTP 요청 본문을 읽고,  
JSON 데이터(Request DTO)를 자바 객체로 변환
- 변환 과정에서 Jackson 라이브러리를 사용해 자동으로 매핑됨(객체 생성)
  - Jackson이 객체를 생성할 때 기본 생성자가 필요
  - 필드 접근을 위해 getter(), setter()가 필요
  - JSON 데이터의 키 이름과 객체 필드명이 일치해야 함

---
# 11강 작성글 저장 1- 게시글 저장 구현
- 컨트롤러, 서비스 , 레포지토리 레이어를 가짐  
- 컨트롤러 -> 서비스 -> 레포지토리를 호출
- 최종적으로 요청 데이터로 넘어온 `JSON`을 `DTO`객체에 매핑하고  
`DTO`를 `Entity`로 변환해서 레포지토리에 저장
  - 드디어 @Builde를 쓰는 이유를 찾은거 같다 .. ! ... !

## @Lob
- 자바에서는 `String`, 데이터베이스에서는 `Long text`형태로 저장
- `@Lob`은 `BLOB`(Binary Large Object), `CLOB`(Character Large Object)와 매핑
```java
    @Lob
    private String content;
```

## 필드 주입과 생성자 주입, 그리고 @RequiredArgsConstructor
### 필드 주입 -> 지양❌
- 왜? 불변성 보장 부족 문제
### 생성자 주입 -> 지향✅
- 왜? 불변성 보장 가능
- 생성자 호출 시 의존성이 반드시 주입되어야 하므로 이후 변경 불가능
### `Lombok`의 `@RequiredArgsConstructor`
  - Lombok이 제공하는 어노테이션
  - `final` 또는 `@NonNull`로 선언된 필드에 대해 자동으로 생성자 생성
  - 생성자 주입의 장점을 그대로 유지하면서 boilerplate 코드를 줄일 수 있음
  - 🪄 **Spring Boot에서 가장 권장되는 의존성 주입 방식**

```java
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
```

```java
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
}
```
## 테스트 코드의 독립성과 반복 가능성
### 독립성
- 각 테스트가 서로 간섭없이 독립적으로 실행될 수 있음을 의미
- 하나의 테스트가 다른 테스트의 실행 결과나 상태에 의존❌
#### 독립성 보장 전략
- 테스트 전에 필요한 모든 상태 초기화
- 테스트 후에는 환경을 전리
- `Mock` 객체 초기화
- 데이터베이스 트랜잭션 롤백
### 반복 가능성
- 테스트를 여러 번 실행해도 항상 동일한 결과를 보장
- 환경과 실행 순서가 달라져도 결과가 일정해야 함
#### 반복 가능성 보장 전략
- `Mock` 객체 활용

### 테스트 코드 독립성과 반복 가능성 보장 전략
1. `Mock`과 `Stub` 활용
- 외부 의존성 제거, 예상된 동작 시뮬레이션
2. JUnit의 `@Before`과 `@After` 사용
- 테스트 전후에 초기화 및 정리 작업 수행
3. 데이터베이스 트랜잭션 관리

📂 `PostControllerTest.java`
```java
    @BeforeEach
    void clean(){
        postRepository.deleteAll();
    }
```
---
# 12강 작성글 저장2 - 클래스 분리
### 테스트 코드의 문제점과 리팩토링
```java
    // ♻️ 테스트 코드 리팩토링
    @Test
    @DisplayName("/posts로 POST 요청 테스크 코드 리팩토링")
    void postTestRefactor() throws Exception{
        // given
        // 생각해볼거리 : 사용자 요청 데이터는 PostCreate(DTO)로 받지 않나? -> 사용하자
        // new를 지양❌ -> new는 생성자 인수 순서를 지켜야 함, 버그 원인 발견이 어려움 -> @Builder 사용
        // PostCreate request = new PostCreate("제목이당", "내용이당");

        PostCreate request = PostCreate.builder()
                .title("제목이당 빌더")
                .content("내용이당 빌더")
                .build();

        // 필드 주입으로 ObejctMapp를 사용
        // ObjectMapper objectMapper = new ObjectMapper();

        // PostCreate를 JSON으로 가공
        String json = objectMapper.writeValueAsString(request);

        System.out.println(json);

        // expected
        mockMvc.perform(MockMvcRequestBuilders.post("/posts")
                .contentType(APPLICATION_JSON)
                .content(json))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("{}"))    // 컨트롤러에서 Map.of()를 반환
                .andDo(MockMvcResultHandlers.print());
    }
```


### ObjectMapper
- ObjectMapper는 Jackson 라이브러리에서 제공하는 클래스
- 자바 객체를 JSON으로 직렬화하거나, JSON을 자바 객체로 역직렬화할 때 사용  
- 스프링에서 기본적으로 `ObjectMapper`에 대한 빈을 제공 _- @Autowired로 주입해서 사용 가능_ 

**1. 직렬화(Serialization) _- writeValueAsString()_**
```java
ObjectMapper objectMapper = new ObjectMapper();

Person person = new Person("John", 30);
String jsonString = objectMapper.writeValueAsString(person);
```
**2. 역직렬화(Deserialization) _- readValue()_**
```java
String jsonString = "{\"name\":\"John\",\"age\":30}";
Person person = objectMapper.readValue(jsonString, Person.class);
```

### @Builder
노션 참고!
- `@Builer`는 내부적으로 빌더 클래스를 만들고, 객체를 생성하는 빌더 메서드를 추가
- `@Builder`는 클래스에 생성자가 없다면, 모든 필드를 초기화하는 `private` 생성자를 생성
- `@Builder`를 클래스에 사용하면  `@AllArgsConstructor`를 대체 가능

#### 빌더의 장점
- 가독성이 좋다
- 값 생성에 대한 유연함 제공
- 필요한 값만 받을 수 있다
- 객체의 불변성 _- 가장 중요_
---
# 13강 게시글 조회1 - 단건 조회

---
# 14강 게시글 조회2 - 응답 클래스 분리
### Request 클래스(`PostCreate`)
- 요청과 요청에 대한 검증(Validation)할 수 있는 정책을 담아둠

### Response 클래스(`PostResponse`)
- 서비스 정책에 맞는 로직이 들어갈 수 있는 클래스
- Post Etity를 바로 JSON을 변환해서 응답❌
- Entity를 Response 클래스로 변환해서 응답⭕

### Entity가 JSON으로 자동으로 변환되어 응답된다구? 
Spring Boot에서는 기본적으로 
  - `@RestController`
  - `@ResponseBody`

가 적용된 메서드에서 Entity 객체를 반환하면 자동으로 JSON으로 변환되어 응답
이 과정은 Jackson 라이브러리를 통해 이루어짐!

### Spring Boot에서 JSON 변환 동작 방식
**1. Repository에서 엔티티를 조회:**
- repository.findById()를 호출하여 데이터베이스에서 엔티티를 가져옴

**2. 컨트롤러 메서드의 반환 값 처리:**
- @RestController 또는 @ResponseBody가 붙은 컨트롤러 메서드는 반환된 객체를 HTTP 응답 바디에 매핑

**3. Jackson을 사용한 직렬화:**
- Spring Boot는 기본적으로 Jackson을 사용하여 반환된 객체를 JSON 형식으로 변환
- 엔티티의 필드를 읽고 JSON 형식의 응답으로 직렬화

---
# 15강 게시글 조회3 - 게시글 여러개 조회
📂 `PostController.java`
```java
    @GetMapping("/posts")
    public List<PostResponse> getAll() {
        return postService.getAll();
    }
```

📂 `PostService.java`
```java
    // 글 조회 메서드 - 전체 조회
    public List<PostResponse> getAll(){
        List<Post> posts = postRepository.findAll();

        // Post를 PostResponse로 변환하는 작업이 필요
        List<PostResponse> postResponses = posts.stream()
                .map(post -> PostResponse.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .build())
                .toList();
        return postResponses;
    }
```
♻️ `PostService.java`
- 반복적으로 작업하는 빌더 코드가 너무 많음
- `PostResponse`에서 생성자 오버로딩을 통해 매개변수로 `Post`를 받음

```java
    public List<PostResponse> getAll(){
        List<Post> posts = postRepository.findAll();

        List<PostResponse> postResponses = posts.stream()
                .map(post -> new PostResponse(post))
                // .map(PostResponse::new)
                .toList();
        return postResponses;
    }
```
📂 `PostResponse.java`
```java
@Getter
public class PostResponse {
    private Long id;
    private String title;
    private String content;

    @Builder
    public PostResponse(Long id, String title, String content) {
        this.id = id;
        // title이 10글자 이하일때 발생하는 에러를 방지하기 위해 Math.min()사용
        this.title = title.substring(0, Math.min(title.length(), 10));
        this.content = content;
    }

    // ♻️ 생성자 오버로딩 - 반복되는 빌더 코드를 줄이기 위함
    public PostResponse(Post post){
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
    }
}
```
## 테스트 코드에서 주목할만한 부분
📂 `PostServiceTest.java`
```java
        // ♻️ 테스트 코드 리팩토링
        postRepository.saveAll(List.of(
                Post.builder()
                        .title("의연 제목1")
                        .content("의연 내용1")
                        .build(),
                Post.builder()
                        .title("한얼 제목1")
                        .content("한얼 제목2")
                        .build()
        ));
```
📂 `PostControllerTest.java`
```java
        // ♻️ 테스트 코드 리팩토링
        Post post1 = postRepository.save(Post.builder()
                .title("의연 제목1")
                .content("의연 내용1")
                .build());

        Post post2 = postRepository.save(Post.builder()
                .title("의연 제목2")
                .content("의연 내용2")
                .build());

        ...
                // 📌 검증 포인트 - json 응답 리스트의 길이, json 응답 객체의 값(title, content 등)
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()", Matchers.is(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(post1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title").value(post1.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value(post1.getContent()))
```
---
# 16강 게시글 조회4 - 페이징 처리

---
# 17강 게시글 조회5 - 페이징 처리(QueryDSL)

---
# 18강 게시글 수정
## 1. 엔티티에 Setter()를 통해 수정
### (1) 게시물 수정 요청 데이터를 PostEdit(DTO)을 통해 받음
- 게시물 수정 요청 데이터를 검증하기 위해 `Validation`을 이용
- 게시물 작성 요청 데이터인 `PostCreate`와 동일
  - `PostCreate`로 퉁칠까? ❌ _- 빠따 쳐맞는 생각_
  - 기능이 다르면 코드가 비슷해도 명확한 분리가 필요
### (2) Post 엔티티에 @Setter를 추가해 setter()를 통한 값 수정
- Post 엔티티에 @Setter 추가
- PostEdit으로 게시글 수정 요청 데이터를 받음
- PostEdit의 값을 꺼내와서(getter) Post 값을 수정(setter)

📂`Post.java`
```java
@Entity
@Getter 
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Lob
    private String content;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

📂`PostEdit.java`
```java
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
```

📂`PostService.java`

```java
    public void edit(Long id, PostEdit postEdit){
        // id를 통해 게시글 하나 가져오기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 엔티티의 setter()를 이용해 게시글 수정
        post.setTitle(postEdit.getTitle());
        post.setContent(postEdit.getContent());
        
        postRepository.save(post);
    }
```

### ❌ 엔티티에 @Setter 사용 지양 !!❌

---

## 2. 엔티티에  @Setter를 제거하고, 변경 메서드 추가
📂`Post.java`
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    @Lob
    private String content;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // 📌 title, content 변경 메서드 추가
    public void change(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```
📂`PostService.java`
```java
    @Transactional
    public void edit(Long id, PostEdit postEdit){
        // id를 통해 게시글 하나 가져오기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        
        // 2. 엔티티에 @Setter를 제거하고, 제목과 내용을 변경하는 메서드 추가
        post.change(postEdit.getTitle(), postEdit.getContent());
    }
}
```
### ⚠️ change()메서드 매개변수의 순서가 바뀌어서 들어온다면?
title에 content저장, content에 title이 저장되는 일 발생  
-> 이런 상황은 버그를 발견하기 굉장이 힘든 문제

### ⚠️ change()메서드 매개변수의 순서가 많아진다면?
실수할 확률이 높아짐

--- 

## 3. 엔티티에 change() 메서드를 제거하고 빌더 클래스 이용
### (1) 클라이언트가 PATCH /posts/{postdId}로 요청을 보냄
### (2) PostEdit 객체로 데이터를 전달받아 검증
### (3) `edit` 서비스 메서드에서
1. 해당 id로 `Post` 엔티티를 조회
2. 엔티티에서 `PostEditorBuilder`를 생성
3. 필요한 값만 수정 후 `PostEditor`를 빌드
4. 엔티티의 `edit` 메서드를 호출해 값을 변경

📂`PostEditor.java`
- `PostEditor`는 수정할 수 있는 필드들에 대해서만 정의
```java
@Getter
public class PostEditor {
    private final String title;
    private final String content;

    @Builder
    public PostEditor(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

📂`Post.java` 
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // @Lob : 자바에서는 String, DB에서는 Long text형태로 되도록 함
    @Lob
    private String content;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    // 📌 빌드하지 않은 빌더 클래스 자체를 반환 - 픽스되지 않은 데이터
    // 📌 현재 이 Post 엔티티가 가지고 있는 값을 그대로 복사해서 넘겨줌
    public PostEditor.PostEditorBuilder toEditor(){
        return PostEditor.builder()
                .title(title)
                .content(content);
    }

    // 📌 값이 픽스된 PostEditor가 넘어옴
    // ✅ 1. PostEditor 딱 한개만 인자로 받는 메서드로 개선 가능
    // ✅ 2. PostEditor내에 수정 가능한 필드만 좁혀서 선언 가능
    public void edit(PostEditor postEditor){
        this.title = postEditor.getTitle();
        this.content = postEditor.getContent();
    }
}
```

📂`PostService.java`
```java
    @Transactional
    public void edit(Long id, PostEdit postEdit){
        // id를 통해 게시글 하나 가져오기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        
        // 📌 PostEditor와 빌드되지 않은 빌더 클래스를 이용
        PostEditor.PostEditorBuilder postEditorBuilder = post.toEditor();
        
        PostEditor postEditor = postEditorBuilder.title(postEdit.getTitle())
                .content(postEdit.getContent())
                .build(); // 📌값을 변경하고 빌드 (값을 픽스시킴)

        post.edit(postEditor);
    }
```
--- 

## 💡생각해볼 거리
- 엔티티에 수정 가능한 필드가 여러 개 있는데, 그 중에서도 일부만 수정한다면?
- 수정하지 않은 수정 가능한 필드는 원래의 값을 유지해야 하는데 어떻게 유지함?
- 만약 클라이언트에서 수정하지 않은 필드에 `null`값을 보내준다면?

📂`PostService.java`
```java
post.edit(
        postEdit.getTitle() != null ? postEdit.getTitle() : post.getTitle(),
        postEdit.getContent() != null ? postEdit.getContent() : post.getContent() 
        );
```
-> 근데 이런짓을 하기 싫어서 `PostEditor`를 사용

---
# 19강 게시글 수정(오류 수정, 보충 내용)
## 기존의 게시글 수정 방식
- 저장되어 있는 게시글(`Post`)을 레포지토리에서 조회
- 요청받은 데이터(`PostEdit`)를 덮어씌우기(`PostEditor`)해서 저장

## 새로운 게시글 수정 방식 
- `PostEditor` 사용❌

  📂`Post.java`
```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    // @Lob : 자바에서는 String, DB에서는 Long text형태로 되도록 함
    @Lob
    private String content;

    @Builder
    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }
    public void edit(String title, String content){
        this.title = title;
        this.content = content;
    }
}
```
📂`PostService.java`
```java
    // 게시글 수정 - 수정해야 할 게시글 식별번호(pk)와 수정할 내용(PostEdit) 필요
    @Transactional
    public void edit(Long id, PostEdit postEdit){
        // id를 통해 게시글 하나 가져오기
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        
        post.edit(postEdit.getTitle(), postEdit.getContent());
    }
}
```


## 왜 PostEditor를 사용해야할까?
- `Post`와 `PostEdit`의 필드가 무수히 늘어나게 된다면?

📂`PostService.java`
```java
    @Transactional
public void edit(Long id, PostEdit postEdit){
    
        Post post = postRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 💡 edit() 메서드에 전달해야되는 매개변수도 무수히 늘어나면 곤란하지 않을까?
        post.edit(postEdit.getTitle(), postEdit.getContent(), ........ , );
        }
```
-> `Post`엔티티 자체에 대해 수정할 수 있는 필드의 범위를 좁히는 것이 중요하지 않을까?  
-> `Post`엔티티에서 수정할 수 있는 제한 범위를 두기 위해 `PostEditor`를 사용

### @Builder의 .build()는 어떻게 작동할까?
`.build()`를 하는 순간 @` Builder` 어노테이션이 달린 생성자 호출?
~~몬말임 ...~~


- 기존에 `Post`에 저장된 데이터를 `PostEditorBuilder`를 통해 가져옴  
이때 `.build()`를 호출하지 않음
- ⚠️`.build()`를 호출하지 않으면 `PostEditor`의  생성자가 호출되지 않음
  - 그래서 `PostEditor`가 아닌 `PostEditorBuilder`가 반환됨

📂`Post.java`
```java
    public PostEditor.PostEditorBuilder toEditor(){
        return PostEditor.builder()
                .title(title)
                .content(content);
    }
```
📂`PostEditior.java`
```java
@Getter
public class PostEditor {
    private final String title;
    private final String content;
    
    @Builder
    public PostEditor(String title, String content) {
        this.title = title;
        this.content = content;
    }
}

```

그리고 서비스에서 title과 content를 덮어씌우고 빌드를 함

📂`PostService.java`
```java
        PostEditor postEditor = postEditorBuilder.title(postEdit.getTitle())
                .content(postEdit.getContent())
                .build();// 값을 변경하고 빌드 (값을 픽스시킴)
```
## 그.런.데
- `null`값 체크는 `PostEditor`가 아니라 `PostEditorBuilder`인 빌더클래스에서 체크  
- `null`값은 `PostEdito`r 생성자가 아니라 빌더 클래스에 들어감
- 그래서 `if` 조건문을 안타게됨  
- 그리고 `PostEditor`의 기본값 `null`이 들어가짐   

📂`PostEditor.java`
```java
@Getter
public class PostEditor {

    private String title;
    private String content;

    @Builder
    public PostEditor(String title, String content) {
        // ❌이 짓거리가 의미없다구 !!❌
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }
}
```
어쨋든 빌더 녀석은 `PostEditor`가 아니라 `PostEditorBuilder`에서 들어가므로  
`PostEditor`에서 `null`값 체크를 하는건 의미가 없음❗❗❌

## 정리
## 🪄 null값 체크는 PostEditor가 아닌 PostEditBuilder에서 해야된다!


## 해결방법
1. Builder 클래스를 수동으로 따로 만들어주는게 좋음
- `build` 패키지에서 PostEditor 내부에 빌드된 Builder클래스랑 메서드를 쌔벼옴
  - `Lombok`이 생성해준 Builder클래스
2. Builder 클래스에 값이 들어갈 때 `null` 체크를 하도록 수정
- PostEditorBuilder에서 조건을 걸어줌

📂`PostEditor.java`
```java
@Getter
public class PostEditor {
    private String title;
    private String content;
    
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
        
        // 📍title - null값 체크
        public PostEditorBuilder title(final String title) {
            if (title != null){
                this.title = title;   
            }
            return this;
        }
        
        // 📍content - null값 체크
        public PostEditorBuilder content(final String content) {
            if (content != null){
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
```

### 근데 public PostEditorBuilder title(final String title) {..} 이거 뭐임?
### 매개변수의 final
- 매개변수에 `final`을 사용하면 해당 매개변수의 참조를 메서드 내에서 변경할 수 없도록 제한
- 단, `final`매개변수가 참조하는 객체의 내부 상태(객체 필드)는 변경 가능
```java
public PostEditorBuilder title(final String title) {
    if (title != null) {
        this.title = title;
    }
    return this;
}
```
- 매개변수 `title`은 메서드 내부에서 변경❌
- `this.title`은 클래스의 필드, 메서드 매개변수로 전달된 `title`의 값을 받아 설정

---
# 20강 게시글 삭제
📂`PostService.java`
```java
    public void delete(Long id){
        // id로 게시글 조회
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        postRepository.deleteById(id);
        // 📍 또는 postRepository.delete(post);
    }
```
📂`PostController.java`
```java
    @DeleteMapping("/posts/{postsId}")
    public void delete(@PathVariable Long postsId) {
        postService.delete(postsId);
    }
```

---
# 21강 예외처리1

---
# 22강 예외처리2

