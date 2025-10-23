package com.algolia.fetcher.services;

import com.algolia.fetcher.config.JwtConfiguration;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtConfiguration jwtConfiguration;

    public String generateJWT(String username, Map<String, Object> claims) {
        var key = jwtConfiguration.getJwtSecretKey();
        var algorithm = jwtConfiguration.getAlgorithm();

        var header = new JWSHeader(algorithm);

        var claimsSet = buildClaimsSet(claims);
        var jwt = new SignedJWT(header, claimsSet);

        try {
            var signer = new MACSigner(key);
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new InternalAuthenticationServiceException(
                    "Failed to generate JWT token for " + username + ".", e);
        }

        return jwt.serialize();
    }

    private JWTClaimsSet buildClaimsSet(Map<String, Object> claims) {
        var issuer = jwtConfiguration.issuer();
        var issuedAt = Instant.now();
        var expirationTime = issuedAt.plus(jwtConfiguration.expiresIn());

        var builder = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expirationTime));

        claims.forEach(builder::claim);

        return builder.build();
    }

}