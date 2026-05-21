# Re-Link 백엔드

Re-Link 백엔드 서비스입니다.

## 문서

- [소셜 로그인 구현 로드맵](docs/social-login-roadmap.md)

## 소셜 로그인 시퀀스

```mermaid
sequenceDiagram
    autonumber
    actor User as 사용자
    participant App as 프론트 앱
    participant Provider as 소셜 Provider SDK
    participant API as Re-Link 백엔드
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
