# 모해 백엔드

모해 백엔드 서비스입니다.

## 문서

- [소셜 로그인 구현 로드맵](docs/social-login-roadmap.md)

## 소셜 로그인 시퀀스

```mermaid
sequenceDiagram
    autonumber
    actor User as 사용자
    participant App as 프론트 앱
    participant Provider as 소셜 Provider SDK
    participant API as 모해 백엔드
    participant Resolver as SocialUserResolver
    participant Member as 회원 저장소
    participant JWT as TokenIssuePort

    User->>App: 소셜 로그인 버튼 클릭
    App->>Provider: Provider SDK로 로그인
    Provider-->>App: idToken 또는 accessToken 반환
    App->>API: POST /api/v1/auth/social/login
    API->>API: SocialLoginCommand 생성
    API->>Resolver: resolve(command)

    alt Kakao
        Resolver->>Provider: accessToken으로 /v2/user/me 호출
        Provider-->>Resolver: 카카오 사용자 정보 반환
    else Google 또는 Apple
        Resolver->>Provider: ID Token 서명과 클레임 검증
        Provider-->>Resolver: 검증된 토큰 클레임
    end

    Resolver-->>API: SocialLoginUserInfo
    API->>Member: findByProviderAndProviderId(provider, providerId)

    alt 기존 회원
        Member-->>API: 회원 반환
    else 신규 회원
        API->>Member: 회원 저장
        Member-->>API: 저장된 회원 반환
    end

    API->>JWT: issue(member)
    JWT-->>API: AuthTokens
    API-->>App: memberId, accessToken, expiresIn
```

## Web Push

### Environment variables

```properties
WEB_PUSH_ENABLED=true
WEB_PUSH_SUBJECT=mailto:admin@example.com
WEB_PUSH_PUBLIC_KEY=your-vapid-public-key
WEB_PUSH_PRIVATE_KEY=your-vapid-private-key
NOTIFICATION_DEDUP_PREFIX=notification:dedup
NOTIFICATION_DEDUP_TTL_SECONDS=300
NOTIFICATION_OUTBOX_ENABLED=true
NOTIFICATION_OUTBOX_BATCH_SIZE=50
NOTIFICATION_OUTBOX_FIXED_DELAY_MILLIS=60000
DB_SCHEMA=mohae
BATCH_SCHEMA=mohae_backend_batch
SPRING_SQL_INIT_MODE=always
BATCH_DB_URL=jdbc:mysql://localhost:3306/${BATCH_SCHEMA}?createDatabaseIfNotExist=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
BATCH_DB_USER_NAME=root
BATCH_DB_PASSWORD=change-me
```

### VAPID key generation

```bash
npx web-push generate-vapid-keys
```

### APIs

- `GET /api/v1/push-subscriptions/public-key`: returns the VAPID public key.
- `POST /api/v1/push-subscriptions`: registers or refreshes the current user's browser subscription.
- `DELETE /api/v1/push-subscriptions`: disables the current user's subscription by endpoint.
- `POST /api/v1/notifications/test`: enqueues a test Web Push notification to the current user.

### Service worker example

```js
self.addEventListener("push", (event) => {
  const payload = event.data ? event.data.json() : {};
  event.waitUntil(
    self.registration.showNotification(payload.title, {
      body: payload.body,
      data: payload.data || { linkUrl: payload.linkUrl },
    })
  );
});

self.addEventListener("notificationclick", (event) => {
  event.notification.close();
  const linkUrl = event.notification.data?.linkUrl || "/";
  event.waitUntil(clients.openWindow(linkUrl));
});
```

### Notes

- iOS Web Push requires an installed PWA and user permission. Browser support and permission UX differ by iOS version.
- `notification_target` stores Web Push subscriptions.
- `notification` stores requested notification messages.
- `notification_outbox` stores pending Web Push jobs for the scheduler.
- `notification_delivery` stores per-target send results.
- Spring Batch metadata tables use the `mohae_backend_batch` schema by default.
- `batch.datasource.*` connects to `mohae_backend_batch` and initializes Batch metadata tables from `db/batch-schema-mysql.sql`.
- `batch.datasource.*` is used only for Spring Batch metadata.
- Check Batch metadata with `show tables from mohae_backend_batch like 'BATCH_%';`.
- When Redis deduplication fails, sending continues and only a warning is logged.
