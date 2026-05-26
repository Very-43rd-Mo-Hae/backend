# 소셜 로그인 구현 현황

## 현재 진입점

앱 SDK 기반 소셜 로그인은 아래 API를 사용합니다.

```http
POST /api/v1/auth/social/login
```

현재 흐름은 다음과 같습니다.

```text
SocialLoginController
 -> SocialLoginService
 -> SocialUserResolver
 -> OAuth2LoginService
 -> Member lookup or signup
 -> service JWT issue
```

회원 조회 기준은 `email`이 아니라 `provider + providerId`입니다.

## Provider 상태

- Kakao: `KakaoSocialUserResolver`에서 SDK access token으로 사용자 정보 API를 호출합니다.
- Google: `GoogleUserResolver`에서 SDK ID token을 검증합니다.
- Apple: 프론트에서 진입을 막으며, 백엔드 resolver는 없습니다.

## 정리 완료

- 임시 resolver 제거
  - `TemporaryKakaoSocialUserResolver`
  - `TemporaryGoogleSocialUserResolver`
  - `TemporaryAppleSocialUserResolver`
  - `AbstractTemporarySocialUserResolver`
- 기존 웹 OAuth2 로그인 플로우 제거
  - `SecurityConfiguration.oauth2Login(...)`
  - `CustomOAuth2UserService`
  - `OAuth2LoginSuccessHandler`
  - `OAuth2LoginFailureHandler`
  - `SpringOAuth2UserClientAdapter`
  - `OAuth2UserInfoFactory`
  - `auth/domain/oauth2/userinfo/*`
  - `spring.security.oauth2.client.*` 설정
  - `spring-boot-starter-security-oauth2-client` 의존성

## 구현 원칙

- 프론트가 보낸 `providerId`, `email`, `name`, `imageUrl`은 신뢰하지 않습니다.
- 프론트는 provider 종류와 provider token만 전달합니다.
- 백엔드는 검증된 token claim 또는 provider user-info API 결과로 사용자 정보를 만듭니다.
- 로그인 식별 기준은 반드시 `provider + providerId`로 유지합니다.
- provider별 검증 로직은 `SocialUserResolver` 구현체 안에 둡니다.

## 나중에 Apple을 열 때

Apple 로그인을 다시 열 때는 임시 resolver 없이 실제 `AppleSocialUserResolver`를 추가해야 합니다.

필수 검증 항목은 다음과 같습니다.

```text
Apple JWKS 서명
iss = https://appleid.apple.com
aud = 설정된 iOS Bundle ID 또는 Service ID
exp
```

검증된 claim은 다음처럼 매핑합니다.

```text
sub -> providerId
email -> email
request.name -> 앱이 최초 로그인 때 전달한 경우에만 name
imageUrl -> null
```
