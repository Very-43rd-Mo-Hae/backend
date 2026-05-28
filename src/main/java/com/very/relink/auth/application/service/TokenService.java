package com.very.relink.auth.application.service;

import com.very.relink.auth.adapter.in.token.ReIssueTokenRequest;
import com.very.relink.auth.application.port.out.RefreshTokenIssuePort;
import com.very.relink.auth.application.result.ReissueTokenResponse;
import com.very.relink.auth.exception.TokenErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private RefreshTokenIssuePort refreshTokenIssuePort;

    /**
     * 1. Refresh Token 서명 검증
     * 2. Refresh Token 만료 검증
     * 3. type이 REFRESH인지 확인
     * 4. payload에서 memberId, sessionId, refreshTokenJti 추출 (완료)
     * 5. Redis refresh:{sessionId} 조회
     * 6. Redis에 값이 없으면 실패
     * 7. 요청 refreshToken을 hash 처리
     * 8. Redis에 저장된 refreshTokenHash와 비교
     * 9. DB auth_session 조회
     * 10. DB session 상태가 ACTIVE인지 확인
     * 11. 새 accessToken 발급
     * 12. 새 refreshToken 발급
     * 13. 새 refreshTokenHash 생성
     * 14. Redis refresh:{sessionId} 값 갱신
     * 15. DB auth_session의 refreshTokenJti, refreshTokenHash, lastUsedAt 갱신
     * 16. 새 토큰 응답
     * @param reIssueTokenRequest RefreshToken을 담은 DTO
     * @return 재발급 AccessToken 정보
     */
    public ReissueTokenResponse reIssueToken(
            ReIssueTokenRequest reIssueTokenRequest
    ) {
        String refreshToken = reIssueTokenRequest.refreshToken();

        if(refreshToken == null || refreshToken.isBlank()) {
            throw TokenErrorCode.REFRESH_TOKEN_NOT_FOUND.toException();
        }

        refreshTokenIssuePort.authenticateRefreshToken(refreshToken);

        return null;
    }
}
