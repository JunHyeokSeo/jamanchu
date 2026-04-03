# taste-match 코드 리뷰

**리뷰어**: 시니어 백엔드 개발자 관점  
**날짜**: 2026-04-02

---

## 수정 완료 항목

| # | 항목 | 상태 |
|---|------|------|
| 1 | 미사용 `mypage.html` 삭제 | 완료 |
| 2 | `MatchController`에서 `calculateMatches` 호출 제거, `FeedController.react()`에서 리액션 후 트리거 | 완료 |
| 3 | `ChatMessageResponse` DTO 생성, `/chat/{matchId}/messages`에서 엔티티 직접 반환 제거 | 완료 |
| 4 | `PostRequest`에 `@NotBlank` 추가, `FeedController`에 `@Valid` 추가, 리액션 enum 변환 예외 처리 | 완료 |
| 5 | `GlobalExceptionHandler` 추가 (`IllegalArgumentException`→400, `IllegalStateException`→409, `Exception`→500) | 완료 |
| 6 | `MyPageController`에서 `ReactionRepository` 직접 의존 제거, `PostResponse.reactionCount` 활용 | 완료 |
| 7 | `MatchService.calculateMatches()`에서 자기 자신과 매칭 방지 guard 추가 | 완료 |

---

## 발견된 코드 품질 이슈

### 1. N+1 쿼리 문제 (High)

**파일**: `MatchService.getMyMatches()`

매칭 목록을 불러올 때 각 `TasteMatch`에 대해 `chatMessageRepository.findByTasteMatchOrderByCreatedAtAsc(match)` 를 개별 호출한다. 매칭이 N개면 N번의 추가 쿼리 발생.

**개선안**: `@EntityGraph` 또는 fetch join 쿼리로 한 번에 로드. 또는 lastMessage/unreadCount를 별도 쿼리(IN 조건)로 일괄 조회.

---

### 2. N+1 쿼리 문제 (High)

**파일**: `FeedController.feed()`, `MyPageController.myPage()`

`postService.getPostResponse(post, currentUser)` 를 각 Post마다 반복 호출하며, 내부에서 `reactionRepository.findByPost(post)` 를 개별 실행한다. Post가 N개면 N번 쿼리.

**개선안**: Post ID 목록으로 리액션을 일괄 조회(`findByPostIn`)한 뒤 메모리에서 그룹핑.

---

### 3. 트랜잭션 경계 문제 (Medium)

**파일**: `MatchService.calculateMatches()`

`@Transactional` 내에서 `reactionRepository.findByReactor()` → `findByPostIn()` → `tasteMatchRepository.findByUser1AndUser2()` (루프 내)를 반복 호출한다. 루프 내 DB 조회는 트랜잭션 내에서 매 반복마다 실행되어 성능 저하.

**개선안**: 루프 전에 기존 매칭을 일괄 조회해 Set으로 캐싱 후 루프에서 메모리 비교.

---

### 4. 입력값 검증 미흡 (Medium)

**파일**: `ChatController.sendMessage()`, `PostRequest`

- `sendMessage`의 `content` 파라미터에 길이 제한 없음. DB 컬럼은 `length = 2000`이지만 컨트롤러 레벨 검증 없음.
- `PostRequest.content`에 `@Size(max=500)` 같은 길이 제한 미적용.

**개선안**: `@NotBlank @Size(max=2000)` 검증 추가. `ChatMessageRequest` DTO가 존재하지만 `sendMessage`에서 사용되지 않고 `@RequestParam String content`로 받음 — DTO로 통일 권장.

---

### 5. ChatMessageRequest DTO 미사용 (Low)

**파일**: `ChatController.sendMessage()`

`dto/ChatMessageRequest.java`가 존재하지만 컨트롤러에서 `@RequestParam String content`를 직접 사용. DTO가 사실상 dead code.

**개선안**: `sendMessage`를 `@RequestBody ChatMessageRequest` (REST) 또는 `@ModelAttribute ChatMessageRequest` (form) 방식으로 통일하거나 DTO 삭제.

---

### 6. 매칭 알고리즘 - 본인이 작성한 Post에 리액션한 경우 (Medium)

**파일**: `MatchService.calculateMatches()`

사용자가 자신의 Post에 리액션을 달 수 있다면, 해당 Post를 기준으로 타인의 리액션과 겹쳐 매칭 점수가 올라갈 수 있다. 실질적으로는 UI에서 막힐 수 있으나 서비스 레이어에 방어 로직 없음.

**개선안**: `myReactedPosts`에서 `post.getAuthor().getId().equals(user.getId())`인 Post 제외.

---

### 7. AuthInterceptor - 인증 우회 가능성 (Low)

**파일**: `AuthInterceptor.java`

세션 토큰을 쿠키에서 읽어 `AnonUser`를 설정하는 구조는 합리적이나, 만약 인터셉터가 적용되지 않는 경로가 있다면 `currentUser`가 null인 채로 컨트롤러 로직이 실행될 수 있다. NPE 발생 위험.

**개선안**: 각 컨트롤러 메서드에서 `currentUser != null` 체크 또는 인터셉터 패턴을 `/**`로 일원화.

---

### 8. `@Transactional(readOnly = true)` 누락 (Low)

**파일**: `PostService.getMyPosts()` 등 조회 메서드들

일부 조회 메서드에는 `readOnly = true`가 적용되어 있으나 일관성이 없음.

**개선안**: 모든 조회 전용 메서드에 `@Transactional(readOnly = true)` 적용으로 성능 최적화 및 의도 명확화.

---

## 빌드 결과

```
BUILD SUCCESSFUL in 1s
```

모든 수정 후 `./gradlew build` 성공 확인.
