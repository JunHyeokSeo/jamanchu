# taste-match QA 테스트 리포트

**작성일**: 2026-04-03  
**테스터**: qa-tester (Claude Agent)  
**테스트 환경**: macOS Darwin 24.6.0, Spring Boot 3.4.4, H2 파일 DB  
**앱 실행**: `./gradlew bootRun` (localhost:8080)

---

## 테스트 결과 요약

| 카테고리 | 상태 |
|---|---|
| 랜딩 페이지 / 가입 | PASS |
| 피드 / 카테고리 필터 | PASS |
| 게시물 작성 | PASS (단, 빈 content → BUG) |
| 리액션 토글 | PASS |
| 매칭 생성 | PASS |
| 매칭 페이지 UI (수락/거절 버튼) | PASS |
| 매칭 수락 | PASS |
| 채팅 (HTML 페이지) | PASS |
| 채팅 JSON API | PASS (재시작 후 정상, 이전에는 BUG) |
| 마이페이지 통계 | MINOR BUG |
| 인증 리다이렉트 (쿠키 없음) | PASS |
| 엣지 케이스 - 존재하지 않는 post 리액션 | PASS (400 반환) |
| 엣지 케이스 - 미수락 매칭 채팅 접근 | PASS (302 redirect) |
| 엣지 케이스 - 타 유저 매칭 수락 | PARTIALLY (400 반환, 명확한 에러 메시지 없음) |

---

## 발견된 버그

### BUG-001 [Critical] 빈 content 게시물 작성 시 HTTP 500

**심각도**: Critical  
**재현 경로**: `POST /posts`  

**재현 방법**:
```bash
curl -si -X POST http://localhost:8080/posts \
  -b "TASTE_TOKEN=<valid_token>" \
  --data "content=&category=음식"
```

**실제 결과**: HTTP 500 Internal Server Error  
**기대 결과**: HTTP 400 또는 폼 유효성 검사 오류 메시지 표시 후 리다이렉트  

**원인 분석**: `PostService.createPost()`에서 `content`가 빈 문자열일 때 DB `NOT NULL` 제약 또는 애플리케이션 단에서 유효성 검사 없이 저장을 시도하여 예외 발생. `PostRequest` DTO에 `@NotBlank` 같은 유효성 검사 어노테이션이 없거나, 컨트롤러에서 `@Valid` 적용이 누락된 것으로 추정.

---

### BUG-002 [Critical] GET /chat/{matchId}/messages JSON API 간헐적 HTTP 500

**심각도**: Critical  
**재현 경로**: `GET /chat/{matchId}/messages`  

**재현 방법**:
```bash
# 매칭 수락 후 메시지 전송
curl -X POST http://localhost:8080/chat/{matchId}/send \
  -b "TASTE_TOKEN=<token>" --data "content=안녕"

# JSON API 호출
curl -s -b "TASTE_TOKEN=<token>" http://localhost:8080/chat/{matchId}/messages
```

**실제 결과**: 첫 번째 앱 실행 세션에서 HTTP 500 `{"error":"Internal Server Error"}`  
**기대 결과**: `[{"id":1,"senderNickname":"...","content":"...","createdAt":"..."}]` JSON 반환  

**원인 분석**: `ChatController.getMessages()`에서 `ChatService.getMessages()`가 반환하는 `List<ChatMessage>`의 `sender` 필드가 `@ManyToOne(fetch = FetchType.LAZY)`로 지연 로딩 설정되어 있음. 컨트롤러에서 `msg.getSender().getNickname()`에 접근할 때 트랜잭션이 이미 종료되어 `LazyInitializationException` 발생으로 추정.

`ChatMessageResponse`로 매핑하는 스트림이 `@Transactional` 범위 밖(컨트롤러)에서 실행되어 lazy-loaded 관계 접근 실패.

**참고**: 앱 재시작 후에는 정상 동작 확인됨 (이유 불명확 - H2 캐시 또는 Hibernate 2nd level cache 관련일 가능성).

---

### BUG-003 [Major] 빈 채팅 메시지가 DB에 저장됨

**심각도**: Major  
**재현 경로**: `POST /chat/{matchId}/send`  

**재현 방법**:
```bash
curl -si -X POST http://localhost:8080/chat/2/send \
  -b "TASTE_TOKEN=<valid_token>" --data "content="
```

**실제 결과**: HTTP 302 (성공으로 처리), DB에 `content = ""` 인 메시지 저장됨  
JSON 확인: `{"id":4,"senderNickname":"취향인#3709","senderId":3,"content":"","createdAt":"2026-04-03T..."}`  
**기대 결과**: HTTP 400 또는 빈 메시지 거부  

**원인 분석**: `ChatService.sendMessage()`에 내용 유효성 검사 없음. `ChatMessage` 엔티티의 `content` 컬럼에 `nullable = false`만 설정되어 있으나 빈 문자열은 허용.

---

### BUG-004 [Major] 매칭 페이지에서 상태 배지 및 액션 버튼 미표시 (특정 조건)

**심각도**: Major  
**재현 경로**: `GET /matches`  

**재현 조건**: User1이 자신이 작성한 게시물에 SAME_HERE/EMPATHY 리액션을 달고 다른 유저도 같은 게시물에 리액션을 달았을 때, 매칭이 생성되더라도 PENDING 상태 배지(`새로운 매칭!`)와 수락/거절 버튼이 렌더링되지 않는 케이스 발생.

**재현 방법**:
1. User1으로 게시물 작성 (자신 소유 게시물)
2. User1이 자신의 게시물에 SAME_HERE 리액션 (게시물 24, 25, 26)
3. User2가 같은 게시물에 SAME_HERE/EMPATHY 리액션
4. `GET /matches` 접근 → 파트너 닉네임은 보이나 수락/거절 버튼 없음

**실제 결과**: 파트너 닉네임과 스코어는 표시되나, 상태 배지(`새로운 매칭!`)와 수락/거절 form이 렌더링되지 않음  
**기대 결과**: PENDING 상태 배지와 수락/거절 버튼 표시

**원인 분석**: `matches.html` 템플릿의 `match.commonCategories` 참조 문제로 추정. `MatchResponse` DTO에 `commonCategories` 필드가 없으나 템플릿에서 사용됨:
```html
<span th:if="${match.commonCategories != null and !match.commonCategories.isEmpty()}"
      th:each="cat : ${match.commonCategories}" ...>
```
이로 인해 해당 섹션에서 Thymeleaf 평가 오류가 발생해 이후 `th:if="${match.status == 'PENDING'}"` 블록 렌더링에 영향을 줄 가능성. 단, 정상 조건(타인 소유 게시물 리액션)에서는 작동 확인됨.

---

### BUG-005 [Major] 타 유저 매칭에 잘못된 채팅 전송 시 HTTP 400 반환 (400 대신 403 또는 리다이렉트 필요)

**심각도**: Major (UX/보안 관점)  
**재현 경로**: `POST /chat/{matchId}/send`  

**재현 방법**:
```bash
# matchId가 자신이 속하지 않은 매칭인 경우
curl -si -X POST http://localhost:8080/chat/2/send \
  -b "TASTE_TOKEN=<token_not_in_match2>" --data "content=테스트"
```

**실제 결과**: HTTP 400 Bad Request (HTML 에러 페이지)  
**기대 결과**: HTTP 403 Forbidden 또는 `/matches`로 리다이렉트  

**원인 분석**: `ChatService.getMatchForChat()`이 `IllegalArgumentException`을 던지고, Spring Boot가 이를 400으로 처리하지만, GET 채팅 페이지는 `try-catch`로 리다이렉트 처리하는 반면 POST는 예외를 그대로 전파함.

---

### BUG-006 [Minor] 마이페이지 unread 카운트 로직 오류

**심각도**: Minor  
**재현 경로**: `GET /my`의 매칭 카운트, `GET /matches`의 unread 배지  

**원인**: `MatchService.getMyMatches()`에서 unread 카운트를 파트너가 보낸 모든 메시지의 수로 계산:
```java
unreadCount = messages.stream()
    .filter(m -> !m.getSender().getId().equals(user.getId()))
    .count();
```
실제 "읽지 않은" 메시지가 아닌 파트너가 보낸 전체 메시지 수를 반환함. 채팅을 열어 봐도 카운트가 0으로 리셋되지 않음.

---

### BUG-007 [Minor] 마이페이지 게시물 수 aria-label 템플릿 표현식 미처리

**심각도**: Minor (접근성)  
**재현 경로**: `GET /my`  

**실제 결과**: 렌더링된 HTML에 `aria-label="${myPosts != null ? myPosts.size() : 0} + '개 게시물'"` 리터럴 문자열 출력  
**기대 결과**: `aria-label="2개 게시물"` 처럼 실제 값 표시  

**원인**: `th:attr="aria-label=..."` 구문 사용 시 Thymeleaf 표현식이 올바르게 평가되지 않음. `th:attr` 내에서 문자열 연결 구문이 잘못됨.

---

### BUG-008 [Minor] 존재하지 않는 post에 리액션 시 HTTP 400 (에러 페이지 없음)

**심각도**: Minor  
**재현 방법**:
```bash
curl -si -X POST http://localhost:8080/posts/999999/react \
  -b "TASTE_TOKEN=<valid_token>" --data "type=LIKE"
```

**실제 결과**: HTTP 400 (기본 Whitelabel Error Page)  
**기대 결과**: 사용자 친화적인 에러 페이지 또는 피드로 리다이렉트

---

## 정상 동작 확인 항목

1. **랜딩 페이지**: GET `/` → 200 OK, 쿠키 있으면 `/feed`로 302 리다이렉트
2. **익명 가입**: POST `/join` → 302, `TASTE_TOKEN` 쿠키 설정, `/feed`로 리다이렉트
3. **피드 페이지**: GET `/feed` → 200 OK, 게시물 목록 표시
4. **카테고리 필터**: GET `/feed?category=음식` 등 전체 7개 카테고리 모두 200 OK
5. **게시물 작성**: POST `/posts` (유효한 content) → 302, `/feed`로 리다이렉트
6. **리액션 토글**: LIKE, EMPATHY, SAME_HERE, CURIOUS 모두 정상 작동
7. **리액션 삭제**: 같은 리액션 두 번 → 삭제 확인 (count 0으로 복귀)
8. **매칭 생성**: 두 유저가 같은 타인 게시물에 2개 이상 SAME_HERE/EMPATHY 리액션 → `GET /matches` 시 자동 계산 및 생성
9. **매칭 페이지**: PENDING 매칭에 수락/거절 버튼 정상 표시 (정상 조건에서)
10. **매칭 수락**: POST `/matches/{id}/accept` → 302, 상태 ACCEPTED 전환
11. **채팅 페이지**: GET `/chat/{matchId}` → 200, 메시지 목록 표시
12. **채팅 전송**: POST `/chat/{matchId}/send` → 302, 메시지 저장
13. **채팅 JSON**: GET `/chat/{matchId}/messages` → JSON 배열 반환 (재시작 후 정상)
14. **미수락 매칭 채팅 접근**: GET `/chat/{pendingMatchId}` → 302, `/matches`로 리다이렉트 (올바른 거부)
15. **쿠키 없이 보호된 페이지**: `/feed`, `/matches`, `/my`, `/chat/{id}` → 302, `/`으로 리다이렉트
16. **마이페이지**: GET `/my` → 200, 매칭 수 정상 표시

---

## 종합 의견

**매칭 페이지 오류 원인**: 사용자가 보고한 매칭 페이지 진입 오류는 **자신이 작성한 게시물에 리액션을 달았을 때** 발생하는 것으로 추정됨. 매칭 알고리즘 자체는 정상 동작하나, 타인 소유 게시물에 리액션해야 의도한 대로 작동함. 자신의 게시물에 리액션하는 경우 매칭 상태 표시 UI에 버그 있음 (BUG-004).

**우선 수정 권고**:
1. BUG-001: `POST /posts` 빈 content 500 오류 → `@Valid` + `@NotBlank` 추가
2. BUG-002: `GET /chat/{matchId}/messages` 500 오류 → `ChatService.getMessages()`를 Eager fetch 또는 `@EntityGraph` 사용
3. BUG-003: 빈 채팅 메시지 저장 → `ChatService.sendMessage()`에 blank 검사 추가
4. BUG-004: 매칭 페이지 버튼 미표시 → `MatchResponse`에 `commonCategories` 필드 추가 또는 템플릿 수정
