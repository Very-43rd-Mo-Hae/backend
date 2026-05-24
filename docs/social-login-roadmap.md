# 소셜 로그인 구현 로드맵

## 현재 상태

현재 백엔드는 앱 SDK 기반 소셜 로그인을 위한 진입점을 제공합니다.

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

회원 조회 기준은 다음 값입니다.

```text
provider + providerId
```

Apple, Google, Kakao 로그인에서는 이메일이 안정적인 로그인 식별자가 아닙니다. 따라서 `email`이 아니라 `provider + providerId`를 기준으로 회원을 식별해야 합니다.

## 현재 임시 구현

현재는 연동 흐름 확인을 위해 아래 임시 resolver를 사용합니다.

```text
TemporaryGoogleSocialUserResolver
TemporaryAppleSocialUserResolver
```

이 resolver들은 아직 provider token을 실제로 검증하지 않습니다. 전달받은 토큰의 해시값으로 임시 `providerId`를 만들어 로그인 흐름만 확인합니다.

운영 배포 전에는 반드시 실제 provider 검증 resolver로 교체해야 합니다.

Kakao는 `KakaoSocialUserResolver` 초안으로 실제 사용자 정보 API 호출 흐름을 연결했습니다. `TemporaryKakaoSocialUserResolver`는 남겨두되 Spring Bean 등록을 제거했습니다.

## 앞으로 해야 할 일

1. 카카오 실제 resolver 구현

   카카오는 프론트 SDK에서 받은 `accessToken`으로 카카오 사용자 정보 API를 호출합니다.

   ```http
   GET https://kapi.kakao.com/v2/user/me
   Authorization: Bearer {accessToken}
   ```

   응답값은 다음처럼 매핑합니다.

   ```text
   id -> providerId
   kakao_account.email -> email
   kakao_account.profile.nickname -> name
   kakao_account.profile.profile_image_url -> imageUrl
   ```

   실제 `KakaoSocialUserResolver`를 추가한 뒤에는 `TemporaryKakaoSocialUserResolver`를 제거하거나 `@Component`를 제거해야 합니다.

   현재 초안 상태:

   ```text
   KakaoSocialUserResolver
    -> GET social.kakao.user-info-uri
    -> Authorization: Bearer {accessToken}
    -> Kakao response mapping
    -> SocialLoginUserInfo
   ```

   테스트 위치 조언:

   ```text
   src/test/java/com/very/relink/auth/adapter/out/social/kakao/KakaoSocialUserResolverTest.java
   ```

   이 테스트는 outbound adapter 계약 테스트입니다. 실제 Kakao 서버를 호출하지 않고 `MockRestServiceServer`로 응답을 고정해서 `id`, `email`, `nickname`, `profile_image_url` 매핑과 실패 케이스를 검증합니다.

2. 구글 실제 resolver 구현

   구글은 프론트 SDK에서 받은 `idToken`을 백엔드에서 검증해야 합니다.

   필수 검증 항목은 다음과 같습니다.

   ```text
   서명
   iss
   aud = 설정된 Google Client ID
   exp
   ```

   검증된 claim은 다음처럼 매핑합니다.

   ```text
   sub -> providerId
   email -> email
   name -> name
   picture -> imageUrl
   ```

   실제 `GoogleSocialUserResolver`를 추가한 뒤에는 `TemporaryGoogleSocialUserResolver`를 제거하거나 `@Component`를 제거해야 합니다.

3. 애플 실제 resolver 구현

   애플은 프론트 SDK에서 받은 `identityToken`을 백엔드에서 검증해야 합니다.

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

   실제 `AppleSocialUserResolver`를 추가한 뒤에는 `TemporaryAppleSocialUserResolver`를 제거하거나 `@Component`를 제거해야 합니다.

4. provider 검증 설정 추가

   provider 검증에 필요한 설정값을 추가합니다.

   ```yaml
   social:
     google:
       client-id: ${GOOGLE_CLIENT_ID}
     apple:
       audience: ${APPLE_AUDIENCE}
     kakao:
       user-info-uri: https://kapi.kakao.com/v2/user/me
   ```

5. 앱 소셜 로그인 검증 후 기존 웹 OAuth2 플로우 제거

   앱 SDK 기반 로그인이 정상 동작하면 아래 항목은 제거 대상입니다.

   ```text
   SecurityConfiguration.oauth2Login(...)
   CustomOAuth2UserService
   OAuth2LoginSuccessHandler
   OAuth2LoginFailureHandler
   SpringOAuth2UserClientAdapter
   OAuth2UserInfoFactory
   auth/domain/oauth2/userinfo/*
   spring.security.oauth2.client.* application.yaml settings
   ```

## 구현 원칙

- 프론트가 보낸 `providerId`, `email`, `name`, `imageUrl`은 신뢰하지 않습니다.
- 프론트는 provider 종류와 provider token만 전달합니다.
- 백엔드는 검증된 token claim 또는 provider user-info API 결과로 사용자 정보를 만들어야 합니다.
- 로그인 식별 기준은 반드시 `provider + providerId`로 유지합니다.
- provider별 검증 로직은 `SocialUserResolver` 구현체 안에 둡니다.
