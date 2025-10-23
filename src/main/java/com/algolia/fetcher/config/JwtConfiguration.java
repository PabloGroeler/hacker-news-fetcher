package com.algolia.fetcher.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "server.jwt")
public record JwtConfiguration(
        String key,
        String issuer,
        String algorithm,
        @DurationMin(hours = 1) Duration expiresIn
) {
    private static final String DEFAULT_ALGORITHM = "HS256";
    private static final String DEFAULT_ISSUER = "hacker-news-fetcher";
    private static final String DEFAULT_EXPIRATION = "60m";

    @ConstructorBinding
    public JwtConfiguration(
            String key,
            String issuer,
            String algorithm,
            Duration expiresIn
    ) {
        this.key = Objects.requireNonNull(key, "'key' cannot be null. Please set 'server.jwt.key' in application.yml");
        this.issuer = issuer != null ? issuer : DEFAULT_ISSUER;
        this.algorithm = algorithm != null ? algorithm : DEFAULT_ALGORITHM;
        this.expiresIn = expiresIn != null ? expiresIn : Duration.parse("PT" + DEFAULT_EXPIRATION);
    }

    public SecretKey getJwtSecretKey() {
        try {
            return new OctetSequenceKey.Builder(key.getBytes(StandardCharsets.UTF_8))
                    .algorithm(getAlgorithm())
                    .build()
                    .toSecretKey();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JWT secret key", e);
        }
    }

    public JWSAlgorithm getAlgorithm() {
        try {
            return JWSAlgorithm.parse(algorithm);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT algorithm: " + algorithm, e);
        }
    }
}
