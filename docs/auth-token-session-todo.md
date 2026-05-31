# 인증 토큰/세션 작업 정리

`AuthController` 기준 엔드포인트:

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/reissue`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/logout/all`

## 현재 완료된 내용

### 로그인

- `AuthController.login()`에서 `SocialLoginRequest`와 `User-Agent` 헤더를 받아 `SocialLoginCommand`로 변환한다.
- `SocialLoginService`가 provider별 `SocialUserResolver`로 소셜 사용자 정보를 조회한다.
- `OAuth2LoginService.login()`에서 기존 회원 조회 또는 신규 회원 저장을 수행한다.
- 로그인 성공 시 `sessionId`, `refreshTokenJti`를 새로 생성한다.
- access token과 refresh token을 발급한다.
- refresh token claim에 `type=REFRESH`, `sessionId`, `jti`를 포함한다.
- refresh token 원문은 저장하지 않고 BCrypt hash를 생성한다.
- DB `auth_session`에 세션 정보를 저장한다.
- Redis `refresh:{sessionId}`에 refresh token hash를 TTL과 함께 저장한다.
- 로그인 요청의 `deviceId`, `deviceName`, `User-Agent`를 `auth_session`에 저장한다.
- 로그인 서비스 단위 테스트가 일부 존재한다.

### 재발급

- `AuthController.reissue()`에서 refresh token을 요청 body로 받는다.
- `TokenService.reIssueToken()`에서 refresh token 요청 누락과 공백을 검증한다.
- refresh token 서명, 만료, token type을 검증하고 claims를 추출한다.
- Redis `refresh:{sessionId}`에서 저장된 refresh token hash를 조회한다.
- 요청 refresh token과 Redis hash를 비교한다.
- DB `auth_session`을 `sessionId`로 조회한다.
- `memberId`, `sessionId`, `refreshTokenJti` 일치 여부를 확인한다.
- 세션 상태가 `ACTIVE`이고 만료되지 않았는지 확인한다.
- 세션의 회원이 실제 존재하는지 확인한다.
- 새 `refreshTokenJti`를 생성하고 access token, refresh token을 다시 발급한다.
- 새 refresh token hash를 생성한다.
- Redis refresh token hash와 TTL을 갱신한다.
- DB `auth_session.refreshTokenJti`, `refreshTokenHash`, `lastUsedAt`을 갱신한다.
- `ReissueTokenResponse`를 반환한다.
- 재발급 성공 단위 테스트가 일부 존재한다.

### 세션 도메인/저장소

- `AuthSession` 도메인에 `ACTIVE`, `LOGGED_OUT`, `REVOKED`, `EXPIRED` 상태가 있다.
- `AuthSession.logout()`, `revoke()`, `expire()` 상태 변경 메서드가 있다.
- `LoadAuthSessionPort.findBySessionId()`가 구현되어 있다.
- `SaveAuthSessionPort.save()`가 구현되어 있다.
- Redis refresh token 저장/조회 포트가 구현되어 있다.

## 아직 해야 하는 내용

### 1. 로그인 엔드포인트 접근 설정 확인

- `AuthController`의 실제 로그인 URL은 `/api/v1/auth/login`이다.
- 현재 `SecurityConfiguration.AUTH_WHITELIST`에는 `/api/v1/auth/social/login`만 허용되어 있다.
- 로그인 API가 인증 없이 호출되어야 한다면 whitelist를 `/api/v1/auth/login`으로 맞춰야 한다.
- 재발급 API를 refresh token body 기반 공개 API로 둘지, access token 인증이 필요한 API로 둘지 결정해야 한다.
  - 현재 구현은 refresh token 자체를 검증하므로 보통 `/api/v1/auth/reissue`도 permitAll 대상이다.
  - permitAll로 열 경우 refresh token 검증 실패 응답이 공통 에러 형식으로 내려가는지 확인한다.

### 2. 로그인 요청 검증 보강

- `SocialLoginRequest.deviceId`, `deviceName` 검증 기준을 정해야 한다.
  - `deviceId` 필수 여부
  - 최대 길이
  - 공백 문자열 처리
  - 같은 기기 재로그인 시 기존 세션을 유지/교체/동시 허용할지 정책
- provider별 필수 토큰 검증을 명확히 해야 한다.
  - Google: `idToken` 필수
  - Kakao: `accessToken` 필수
  - Apple 추가 시 `idToken`, 최초 로그인 `name` 처리 정책
- `User-Agent` 최대 길이와 초과 시 자르기/실패 정책을 정해야 한다.
- Swagger 설명과 example이 현재 깨져 있으므로 로그인 요청/응답 문구를 복구해야 한다.

### 3. 재발급 보강

- `ReIssueTokenRequest.refreshToken`에 Bean Validation을 붙일지 결정해야 한다.
  - 현재는 서비스에서 null/blank를 직접 검증한다.
- refresh token 재사용 감지 정책을 정해야 한다.
  - 현재 이전 refresh token으로 재발급하면 Redis hash 또는 DB `refreshTokenJti` 불일치로 실패한다.
  - 실패만 할지, 해당 세션을 `REVOKED`로 바꿀지, 회원 전체 세션을 폐기할지 정책이 필요하다.
- Redis 갱신 성공 후 DB 저장 실패, DB 저장 성공 후 Redis 갱신 실패 같은 불일치 상황 처리 전략이 필요하다.
- access token claim에는 현재 `sessionId`가 없다.
  - 로그아웃을 access token 기반으로 처리하려면 access token에도 `sessionId`를 넣는 방식을 검토해야 한다.
  - 또는 로그아웃 요청 body/header로 refresh token을 받아 sessionId를 추출하는 방식으로 정해야 한다.
- 재발급 실패 케이스 테스트를 추가해야 한다.
  - refresh token 없음/공백
  - refresh token 만료
  - Redis hash 없음
  - Redis hash 불일치
  - DB 세션 없음
  - `memberId` 불일치
  - `refreshTokenJti` 불일치
  - 세션 상태가 `ACTIVE`가 아님
  - 세션 만료
  - 회원 없음

### 4. 로그아웃 구현

- `AuthController.logout()`은 현재 `501 NOT_IMPLEMENTED`이다.
- 현재 세션 로그아웃 방식을 먼저 결정해야 한다.
  - 선택지 A: Authorization access token에서 `sessionId`를 읽어 현재 세션 로그아웃
  - 선택지 B: 요청 body로 refresh token을 받아 refresh token claims의 `sessionId`로 현재 세션 로그아웃
- access token 기반으로 갈 경우 필요한 작업:
  - access token claim에 `sessionId` 추가
  - `AuthenticatedMember` 또는 별도 인증 principal에 `sessionId` 포함
  - 컨트롤러에서 인증 사용자/세션 정보를 받을 수 있게 변경
- refresh token 기반으로 갈 경우 필요한 작업:
  - `LogoutRequest(refreshToken)` DTO 추가
  - refresh token 인증 및 sessionId 추출
  - 요청자가 해당 세션 소유자인지 검증
- 공통 구현 작업:
  - `LogoutUseCase` 또는 `TokenService.logout()` 추가
  - `auth_session.status`를 `LOGGED_OUT`으로 변경
  - `auth_session.loggedOutAt` 저장
  - Redis `refresh:{sessionId}` 삭제 포트 추가
  - 이미 로그아웃/만료/폐기된 세션 요청의 응답 정책 결정
  - 로그아웃 후 재발급 실패 테스트 추가

### 5. 전체 로그아웃 구현

- `AuthController.logoutAll()`은 현재 `501 NOT_IMPLEMENTED`이다.
- 현재 로그인 회원의 모든 활성 세션을 조회할 저장소 기능이 필요하다.
  - 예: `findAllByMemberIdAndStatus(memberId, ACTIVE)`
- 전체 로그아웃 처리 방식이 필요하다.
  - 대상 회원의 활성 세션을 `LOGGED_OUT` 또는 `REVOKED`로 변경
  - 각 세션의 Redis `refresh:{sessionId}` 삭제
- access token 기반 인증 사용자에서 `memberId`를 가져오는 컨트롤러/서비스 흐름이 필요하다.
- 일부 Redis 삭제 실패 시 DB 상태와 Redis 상태의 불일치 처리 정책을 정해야 한다.
- 전체 로그아웃 후 모든 기존 refresh token 재발급 실패 테스트가 필요하다.

### 6. 저장소/포트 추가 작업

- Redis 삭제 포트가 필요하다.
  - 예: `DeleteRefreshTokenCachePort.deleteBySessionId(String sessionId)`
  - 전체 로그아웃을 위해 여러 sessionId 삭제를 반복할지, bulk 삭제를 둘지 결정
- `AuthSession` 조회 포트 보강이 필요하다.
  - `findBySessionIdAndMemberId`
  - `findAllByMemberIdAndStatus`
- 세션 상태 변경 저장 시 기존 row update가 의도대로 동작하는지 확인해야 한다.
  - 현재 mapper가 domain을 entity로 다시 만들어 `save()`하므로 id 유지 여부와 createdAt 보존 여부를 테스트로 확인한다.

### 7. Swagger/문서 정리

- `AuthSwagger`의 한글 문구가 깨져 있어 복구가 필요하다.
- 재발급 응답 schema가 현재 `SocialLoginResponse.class`로 지정되어 있어 `ReissueTokenResponse.class`로 맞춰야 한다.
- 로그인, 재발급, 로그아웃, 전체 로그아웃의 인증 필요 여부를 Swagger에 명확히 표시해야 한다.

## 우선순위 제안

1. `SecurityConfiguration`의 로그인/재발급 whitelist를 실제 API 경로에 맞춘다.
2. 로그아웃 기준을 정한다: access token에 `sessionId`를 넣을지, refresh token body를 받을지.
3. Redis refresh token 삭제 포트와 adapter를 추가한다.
4. 현재 세션 로그아웃을 구현하고 테스트한다.
5. 회원 기준 활성 세션 조회를 추가해 전체 로그아웃을 구현하고 테스트한다.
6. 재발급 실패 케이스 테스트와 Swagger 문구를 보강한다.

## 주의사항

- refresh token 원문은 DB, Redis, 로그에 남기지 않는다.
- 인증/인가 정책 변경은 전체 API 접근성에 영향을 주므로 whitelist 변경 시 테스트가 필요하다.
- 로그아웃 구현 전 `sessionId`를 어디서 얻을지 먼저 결정해야 한다.
- refresh token rotation 이후 이전 token 재사용을 단순 실패로 둘지, 세션 탈취 의심으로 폐기할지 정책 결정이 필요하다.
- Redis와 DB를 함께 갱신하는 작업은 실패 시 불일치 가능성이 있으므로 테스트와 운영 로그 기준이 필요하다.
