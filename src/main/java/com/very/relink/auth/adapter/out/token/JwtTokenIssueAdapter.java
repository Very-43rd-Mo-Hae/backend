package com.very.relink.auth.adapter.out.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.very.relink.auth.application.port.out.TokenAuthenticationPort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.domain.token.AuthenticatedMember;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.auth.infra.token.JwtProperties;
import com.very.relink.member.domain.Member;
import java.text.ParseException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenIssueAdapter implements TokenIssuePort, TokenAuthenticationPort {

    private static final String TOKEN_TYPE = "Bearer";

    private final JwtProperties jwtProperties;

    public JwtTokenIssueAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public AuthTokens issue(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(jwtProperties.accessTokenExpirationSeconds());

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(member.getId()))
                .claim("email", member.getEmail())
                .claim("name", member.getName())
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
                TOKEN_TYPE,
                jwtProperties.accessTokenExpirationSeconds()
        );
    }

    @Override
    public AuthenticatedMember authenticate(String accessToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(accessToken);
            boolean verified = signedJWT.verify(new MACVerifier(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
            if (!verified) {
                throw AuthErrorCode.INVALID_JWT_TOKEN.toException();
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            if (claimsSet.getExpirationTime() == null || claimsSet.getExpirationTime().before(new Date())) {
                throw AuthErrorCode.EXPIRED_JWT_TOKEN.toException();
            }

            return new AuthenticatedMember(
                    Long.valueOf(claimsSet.getSubject()),
                    claimsSet.getStringClaim("email"),
                    claimsSet.getStringClaim("name")
            );
        } catch (ParseException | JOSEException | IllegalArgumentException exception) {
            throw AuthErrorCode.INVALID_JWT_TOKEN.toException();
        }
    }
}
