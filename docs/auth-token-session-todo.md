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
- `SecurityConfiguration.AUTH_WHITELIST`에 `/api/v1/auth/reissue`가 포함되어 있어 access token 없이 호출 가능하다.
- `RefreshTokenSessionValidator`에서 refresh token 요청 누락과 공백을 검증한다.
- `RefreshTokenSessionValidator`에서 refresh token 서명, 만료, token type을 검증하고 claims를 추출한다.
- `RefreshTokenSessionValidator`에서 Redis `refresh:{sessionId}`에 저장된 refresh token hash를 조회한다.
- `RefreshTokenSessionValidator`에서 요청 refresh token과 Redis hash를 비교한다.
- `RefreshTokenSessionValidator`에서 DB `auth_session`을 `sessionId`로 조회한다.
- `RefreshTokenSessionValidator`에서 `memberId`, `sessionId`, `refreshTokenJti` 일치 여부를 확인한다.
- 세션 상태가 `ACTIVE`이고 만료되지 않았는지 확인한다.
- 세션의 회원이 실제 존재하는지 확인한다.
- 새 `refreshTokenJti`를 생성하고 access token, refresh token을 다시 발급한다.
- 새 refresh token hash를 생성한다.
- Redis refresh token hash와 TTL을 갱신한다.
- DB `auth_session.refreshTokenJti`, `refreshTokenHash`, `lastUsedAt`을 갱신한다.
- `ReissueTokenResponse`를 반환한다.
- `AuthSwagger`의 재발급 응답 schema가 `ReissueTokenResponse.class`로 지정되어 있다.
- 재발급 성공 단위 테스트가 일부 존재한다.

### 로그아웃

- `AuthController.logout()`에서 `LogoutRequest(refreshToken)`을 요청 body로 받는다.
- `SecurityConfiguration.AUTH_WHITELIST`에 `/api/v1/auth/logout`이 포함되어 있어 access token 없이 호출 가능하다.
- `TokenService.logout()`에서 refresh token 기반으로 현재 세션을 찾는다.
- refresh token 검증은 재발급과 같은 `RefreshTokenSessionValidator`를 재사용한다.
- 세션 상태가 `ACTIVE`이고 만료되지 않았는지 확인한다.
- `auth_session.status`를 `LOGGED_OUT`으로 변경한다.
- `auth_session.loggedOutAt`을 저장한다.
- Redis `refresh:{sessionId}`를 삭제한다.
- 로그아웃 성공 단위 테스트가 일부 존재한다.

### 세션 도메인/저장소

- `AuthSession` 도메인에 `ACTIVE`, `LOGGED_OUT`, `REVOKED`, `EXPIRED` 상태가 있다.
- `AuthSession.logout()`, `revoke()`, `expire()` 상태 변경 메서드가 있다.
- `LoadAuthSessionPort.findBySessionId()`가 구현되어 있다.
- `SaveAuthSessionPort.save()`가 구현되어 있다.
- Redis refresh token 저장/조회 포트가 구현되어 있다.
- Redis refresh token 삭제 포트가 구현되어 있다.

## 아직 해야 하는 내용

### 1. 로그인 요청 검증 보강

- 로그인 API는 현재 `/api/v1/auth/login`이고 whitelist에도 반영되어 있다.
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

### 2. 재발급 보강

- `AuthController.reissue()`의 흐름은 현재 의도한 refresh token rotation 구조와 맞다.
  - access token 인증 없이 refresh token 자체를 검증한다.
  - Redis hash, DB 세션, `refreshTokenJti`, `memberId`, 세션 상태를 모두 확인한 뒤 새 토큰을 발급한다.
- `ReIssueTokenRequest.refreshToken`에 Bean Validation을 붙일지 결정해야 한다.
  - 현재는 서비스에서 null/blank를 직접 검증한다.
- refresh token 재사용 감지 정책을 정해야 한다.
  - 현재 이전 refresh token으로 재발급하면 Redis hash 또는 DB `refreshTokenJti` 불일치로 실패한다.
  - 실패만 할지, 해당 세션을 `REVOKED`로 바꿀지, 회원 전체 세션을 폐기할지 정책이 필요하다.
- Redis 갱신 성공 후 DB 저장 실패, DB 저장 성공 후 Redis 갱신 실패 같은 불일치 상황 처리 전략이 필요하다.
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

### 3. 로그아웃 보강

- `LogoutRequest.refreshToken`에 Bean Validation을 붙일지 결정해야 한다.
  - 현재는 서비스에서 null/blank를 직접 검증한다.
- 이미 로그아웃/만료/폐기된 세션 요청의 응답 정책을 확정해야 한다.
  - 현재 `LOGGED_OUT`, `REVOKED`, 만료 상태별로 다른 `TokenErrorCode`를 던진다.
- 로그아웃 실패 케이스 테스트를 추가해야 한다.
  - refresh token 없음/공백
  - refresh token 만료
  - Redis hash 없음
  - Redis hash 불일치
  - DB 세션 없음
  - 이미 로그아웃된 세션
  - 폐기된 세션
  - 세션 만료
- 로그아웃 후 기존 refresh token으로 재발급이 실패하는지 테스트해야 한다.

### 4. 전체 로그아웃 구현

- `AuthController.logoutAll()`은 현재 `501 NOT_IMPLEMENTED`이다.
- 현재 로그인 회원의 모든 활성 세션을 조회할 저장소 기능이 필요하다.
  - 예: `findAllByMemberIdAndStatus(memberId, ACTIVE)`
- 전체 로그아웃 처리 방식이 필요하다.
  - 대상 회원의 활성 세션을 `LOGGED_OUT` 또는 `REVOKED`로 변경
  - 각 세션의 Redis `refresh:{sessionId}` 삭제
- access token 기반 인증 사용자에서 `memberId`를 가져오는 컨트롤러/서비스 흐름이 필요하다.
- 일부 Redis 삭제 실패 시 DB 상태와 Redis 상태의 불일치 처리 정책을 정해야 한다.
- 전체 로그아웃 후 모든 기존 refresh token 재발급 실패 테스트가 필요하다.

### 5. 저장소/포트 추가 작업

- `AuthSession` 조회 포트 보강이 필요하다.
  - `findBySessionIdAndMemberId`
  - `findAllByMemberIdAndStatus`
- 전체 로그아웃을 위해 Redis 삭제를 여러 sessionId 반복으로 처리할지, bulk 삭제 포트를 둘지 결정해야 한다.
- 세션 상태 변경 저장 시 기존 row update가 의도대로 동작하는지 확인해야 한다.
  - 현재 mapper가 domain을 entity로 다시 만들어 `save()`하므로 id 유지 여부와 createdAt 보존 여부를 테스트로 확인한다.

### 6. Swagger/문서 정리

- 로그아웃 요청/응답 schema와 인증 필요 여부를 Swagger에 명확히 표시해야 한다.
- 전체 로그아웃의 인증 필요 여부를 Swagger에 명확히 표시해야 한다.

## 우선순위 제안

1. 재발급/로그아웃 실패 케이스 테스트를 보강한다.
2. `ReIssueTokenRequest`, `LogoutRequest`의 Bean Validation 적용 여부를 결정한다.
3. 회원 기준 활성 세션 조회를 추가해 전체 로그아웃을 구현하고 테스트한다.
4. 전체 로그아웃의 Redis 삭제 방식을 결정한다.
5. 세션 저장 mapper가 기존 row update, `createdAt` 보존을 의도대로 처리하는지 테스트한다.
6. Swagger에 로그아웃/전체 로그아웃 인증 정책과 요청 schema를 정리한다.

## 주의사항

- refresh token 원문은 DB, Redis, 로그에 남기지 않는다.
- 인증/인가 정책 변경은 전체 API 접근성에 영향을 주므로 whitelist 변경 시 테스트가 필요하다.
- 현재 로그아웃은 refresh token body에서 `sessionId`를 얻는 방식이다.
- refresh token rotation 이후 이전 token 재사용을 단순 실패로 둘지, 세션 탈취 의심으로 폐기할지 정책 결정이 필요하다.
- Redis와 DB를 함께 갱신하는 작업은 실패 시 불일치 가능성이 있으므로 테스트와 운영 로그 기준이 필요하다.
