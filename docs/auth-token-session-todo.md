# 인증 토큰/세션 남은 작업

## 현재 완료된 것

- 로그인 성공 시 `sessionId`, `refreshTokenJti` 생성
- access token, refresh token 발급
- refresh token claim에 `type=REFRESH`, `sessionId`, `jti` 포함
- refresh token 원문 대신 BCrypt hash 생성
- DB `auth_session` 저장
- Redis `refresh:{sessionId}`에 refresh token hash와 TTL 저장
- 로그인 요청에서 `deviceId`, `deviceName` 수신
- `User-Agent` 헤더를 `auth_session.userAgent`에 저장

## 다음 작업

### 1. 토큰 재발급 완성

- `TokenService.reIssueToken()`에서 refresh token claims 추출
- Redis `refresh:{sessionId}` 조회
- 요청 refresh token과 Redis hash 비교
- DB `auth_session` 조회
- `memberId`, `sessionId`, `refreshTokenJti` 일치 여부 확인
- 세션 상태가 `ACTIVE`인지 확인
- 세션 만료 여부 확인
- 새 access token 발급
- 새 refresh token 발급
- 새 refresh token hash 생성
- Redis hash와 TTL 갱신
- DB `auth_session.refreshTokenJti`, `refreshTokenHash`, `lastUsedAt` 갱신
- `ReissueTokenResponse` 반환

### 2. Redis 조회/삭제 포트 추가

- `LoadRefreshTokenCachePort`
  - `findHashBySessionId(String sessionId)`
- `DeleteRefreshTokenCachePort`
  - 단일 세션 삭제
  - 필요 시 전체 로그아웃용 key 삭제 전략 검토

### 3. AuthSession 저장소 기능 보강

- `findBySessionId` 외에 필요한 조회 추가
  - `findBySessionIdAndMemberId`
  - `findAllByMemberIdAndStatus`
- refresh token rotation용 저장/갱신 흐름 테스트 추가
- 전체 로그아웃을 위한 member 기준 세션 상태 변경 방식 결정

### 4. 로그아웃 구현

- 현재 세션 로그아웃
  - refresh token 또는 access token에서 세션 식별 방식 결정
  - DB 세션 상태를 `LOGGED_OUT`으로 변경
  - Redis `refresh:{sessionId}` 삭제
- 전체 로그아웃
  - memberId 기준 활성 세션 전체 `LOGGED_OUT` 또는 `REVOKED`
  - 관련 Redis key 삭제

### 5. 요청 검증

- 로그인 요청의 `deviceId`, `deviceName` 검증 기준 결정
  - `deviceId` 필수 여부
  - 최대 길이
  - 공백 처리
- `User-Agent` 길이 초과 시 잘라낼지 실패시킬지 결정
- Swagger example 최신화

### 6. 테스트 보강

- 재발급 성공 테스트
- refresh token 만료 테스트
- Redis hash 불일치 테스트
- DB 세션 없음 테스트
- 세션 상태가 `ACTIVE`가 아닌 경우 테스트
- refresh token 재사용 감지 테스트
- 로그아웃 후 재발급 실패 테스트

## 주의할 점

- refresh token 원문은 DB, Redis, 로그에 남기지 않는다.
- Redis와 DB 갱신 순서를 정할 때 실패 시 불일치 가능성을 고려한다.
- refresh token rotation 이후 이전 token 재사용은 `REUSED_REFRESH_TOKEN`으로 차단한다.
- 세션 관련 변경은 인증/인가 영향이 크므로 작은 단위로 테스트를 붙여 진행한다.
