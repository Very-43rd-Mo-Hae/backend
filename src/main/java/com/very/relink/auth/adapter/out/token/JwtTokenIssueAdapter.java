package com.very.relink.auth.adapter.out.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.very.relink.auth.application.port.out.RefreshTokenIssuePort;
import com.very.relink.auth.application.port.out.TokenAuthenticationPort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.token.RefreshTokenClaims;
import com.very.relink.auth.exception.TokenErrorCode;
import com.very.relink.auth.infra.token.JwtProperties;
import com.very.relink.member.domain.Member;
import java.text.ParseException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssueAdapter implements TokenIssuePort, TokenAuthenticationPort, RefreshTokenIssuePort {

    private static final String TOKEN_TYPE = "Bearer";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_SESSION_ID = "sessionId";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH";

    private final JwtProperties jwtProperties;

    public JwtTokenIssueAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AuthTokens issue(Member member, String sessionId, String refreshTokenJti) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(member.getId()))
                .jwtID(UUID.randomUUID().toString())
                .claim("email", member.getEmail())
                .claim("name", member.getName())
                .claim(CLAIM_TYPE, ACCESS_TOKEN_TYPE)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        try {
            signedJWT.sign(new MACSigner(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to issue JWT token.", exception);
        }

        return new AuthTokens(
                signedJWT.serialize(),
                issueRefreshToken(member.getId(), sessionId, refreshTokenJti),
                TOKEN_TYPE,
                jwtProperties.accessTokenExpirationSeconds(),
                jwtProperties.refreshTokenExpirationSeconds()
        );
    }

    @Override
    public String issueRefreshToken(Long memberId, String sessionId, String refreshTokenJti) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.refreshTokenExpirationSeconds());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(memberId))
                .jwtID(refreshTokenJti)
                .claim(CLAIM_TYPE, REFRESH_TOKEN_TYPE)
                .claim(CLAIM_SESSION_ID, sessionId)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet
        );

        try {
            signedJWT.sign(new MACSigner(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException exception) {
            throw new IllegalStateException("Failed to issue refresh JWT token.", exception);
        }

        return signedJWT.serialize();
    }

    @Override
    public RefreshTokenClaims authenticateRefreshToken(String refreshToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(refreshToken);
            boolean verified = signedJWT.verify(new MACVerifier(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
            if (!verified) {
                throw TokenErrorCode.INVALID_REFRESH_TOKEN.toException();
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().before(new Date())) {
                throw TokenErrorCode.EXPIRED_REFRESH_TOKEN.toException();
            }

            String tokenType = claimsSet.getStringClaim(CLAIM_TYPE);
            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
                throw TokenErrorCode.INVALID_REFRESH_TOKEN.toException();
            }

            return new RefreshTokenClaims(
                    Long.valueOf(claimsSet.getSubject()),
                    claimsSet.getStringClaim(CLAIM_SESSION_ID),
                    claimsSet.getJWTID()
            );
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            throw TokenErrorCode.INVALID_REFRESH_TOKEN.toException();
        }
    }

    @Override
    public AuthenticatedMember authenticate(String accessToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            boolean verified = signedJWT.verify(new MACVerifier(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
            if (!verified) {
                throw TokenErrorCode.INVALID_ACCESS_TOKEN.toException();
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().before(new Date())) {
                throw TokenErrorCode.EXPIRED_ACCESS_TOKEN.toException();
            }

            return new AuthenticatedMember(
                    Long.valueOf(claimsSet.getSubject()),
                    claimsSet.getStringClaim("email"),
                    claimsSet.getStringClaim("name")
            );
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            throw TokenErrorCode.INVALID_ACCESS_TOKEN.toException();
        }
    }
}
