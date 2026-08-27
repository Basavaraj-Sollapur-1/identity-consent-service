package com.echolife.identity.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class JwtConfig {

    @Value("${echolife.jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${echolife.jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${echolife.jwt.issuer}")
    private String issuer;


    // =========================================================
    // KEY PATH
    // =========================================================

    private Path resolveKeyPath(String configuredPath) {

        Path path = Path.of(configuredPath);

        if (path.isAbsolute()) {
            return path.normalize();
        }

        return Path.of(System.getProperty("user.dir"))
                .resolve(path)
                .normalize();
    }


    // =========================================================
    // PRIVATE KEY
    // =========================================================

    @Bean
    RSAPrivateKey privateKey() throws Exception {

        Path path = resolveKeyPath(privateKeyPath);

        System.out.println("========================================");
        System.out.println("PRIVATE KEY PATH = " + path);
        System.out.println("========================================");

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "Private key file not found: " + path
            );
        }

        String key = Files.readString(path)
                .replace(
                        "-----BEGIN PRIVATE KEY-----",
                        ""
                )
                .replace(
                        "-----END PRIVATE KEY-----",
                        ""
                )
                .replace(
                        "-----BEGIN RSA PRIVATE KEY-----",
                        ""
                )
                .replace(
                        "-----END RSA PRIVATE KEY-----",
                        ""
                )
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return (RSAPrivateKey) KeyFactory
                .getInstance("RSA")
                .generatePrivate(
                        new PKCS8EncodedKeySpec(decoded)
                );
    }


    // =========================================================
    // PUBLIC KEY
    // =========================================================

    @Bean
    RSAPublicKey publicKey() throws Exception {

        Path path = resolveKeyPath(publicKeyPath);

        System.out.println("========================================");
        System.out.println("PUBLIC KEY PATH = " + path);
        System.out.println("========================================");

        if (!Files.exists(path)) {
            throw new IllegalStateException(
                    "Public key file not found: " + path
            );
        }

        String key = Files.readString(path)
                .replace(
                        "-----BEGIN PUBLIC KEY-----",
                        ""
                )
                .replace(
                        "-----END PUBLIC KEY-----",
                        ""
                )
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);

        return (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(
                        new X509EncodedKeySpec(decoded)
                );
    }


    // =========================================================
    // JWT ENCODER
    // =========================================================

    @Bean
    JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey) {

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(
                        new JWKSet(rsaKey)
                )
        );
    }


    // =========================================================
    // MAIN JWT DECODER
    // =========================================================

    @Bean
    @Primary
    JwtDecoder jwtDecoder(RSAPublicKey publicKey) {

        System.out.println("========================================");
        System.out.println("JWT DECODER CONFIGURATION");
        System.out.println("JWT ISSUER = [" + issuer + "]");
        System.out.println("JWT ISSUER LENGTH = " + issuer.length());
        System.out.println("JWT ISSUER BYTES = " +
                java.util.Arrays.toString(
                        issuer.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        )
                ));
        System.out.println("========================================");

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience ->
                                audience != null
                                        && audience.contains("echolife-session")
                );

        OAuth2TokenValidator<Jwt> validator =
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                );

        decoder.setJwtValidator(validator);

        return decoder;
    }


    // =========================================================
    // MFA CHALLENGE JWT DECODER
    // =========================================================

    @Bean(name = "mfaChallengeJwtDecoder")
    JwtDecoder mfaChallengeJwtDecoder(
            RSAPublicKey publicKey) {

        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withPublicKey(publicKey)
                        .build();

        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
                new JwtClaimValidator<List<String>>(
                        "aud",
                        audience ->
                                audience != null
                                        && audience.contains("echolife-identity")
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        issuerValidator,
                        audienceValidator
                )
        );

        return decoder;
    }


    // =========================================================
    // JWT ISSUER BEAN
    // =========================================================

    @Bean
    String jwtIssuer() {
        return issuer;
    }
}