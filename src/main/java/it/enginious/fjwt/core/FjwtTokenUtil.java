package it.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.impl.security.DefaultSecureRequest;
import io.jsonwebtoken.security.MacAlgorithm;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.jsonwebtoken.security.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Jwt token utilities.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FjwtTokenUtil {

    /**
     * The clock
     */
    private final Clock clock;

    /**
     * Fjwt configuration
     */
    private final FjwtConfig fjwtConfig;

    /**
     * Claims extractor chain
     */
    private final FjwtClaimsExtractorChain claimsExtractorChain;

    /**
     * User details builder factory
     */
    private final FjwtUserDetailsBuilderFactory userDetailsBuilderFactory;

    private SecretKey key;

    /**
     * initialize this bean, see {@link PostConstruct}
     */
    @PostConstruct
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void init() {

        String algorithmId = StringUtils.defaultIfBlank(fjwtConfig.getAlgorithm(), Jwts.SIG.HS256.getId());


        SecureDigestAlgorithm algorithm = Jwts.SIG.get().forKey(algorithmId);


        if (Objects.isNull(fjwtConfig.getAlgorithm())) {
            log.warn("no algorithm provided: {} will be used", algorithmId);
        }
        byte[] encoded;
        if (algorithm instanceof MacAlgorithm mac) {
            encoded = mac.key().build().getEncoded();
        } else if (algorithm instanceof SignatureAlgorithm sig) {
            encoded = sig.keyPair().build().getPrivate().getEncoded();
        } else {
            throw new IllegalStateException("algorithm must be one of  HS256, HS384, HS512, RS256, RS384, RS512, PS256, PS384, PS512, ES256, ES384, ES512, EdDSA");
        }

        String generatedSecret =
                new String(
                        Base64.getEncoder().encode(encoded),
                        StandardCharsets.UTF_8);

        if (StringUtils.isBlank(fjwtConfig.getSecret())) {
            log.warn(
                    "no secret provided: {} (generated with algorithm {}) will be used",
                    (log.isTraceEnabled()
                            ? generatedSecret
                            : StringUtils.repeat("*", StringUtils.length(generatedSecret))),
                    algorithmId);
        } else {
            log.info(
                    "secret provided: {} will be used",
                    (log.isTraceEnabled()
                            ? fjwtConfig.getSecret()
                            : StringUtils.repeat("*", StringUtils.length(fjwtConfig.getSecret()))));
        }

        String secret = StringUtils.defaultIfBlank(fjwtConfig.getSecret(), generatedSecret);

        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), toJcaName(algorithmId));
        log.info("validating key strength");
        try {
            try (InputStream is = new ByteArrayInputStream(key.getEncoded())) {
                algorithm.digest(new DefaultSecureRequest<>(is, null, null, key));
            }
        } catch (Exception e) {
            throw new IllegalStateException("fjwt key validation failed", e);
        }
    }

    /**
     * Parse token and return the username
     *
     * @param token the token
     * @return the username
     * @throws MalformedJwtException    if the specified JWT was incorrectly constructed (and therefore
     *                                  invalid). Invalid JWTs should not be trusted and should be discarded.
     * @throws SignatureException       if a JWS signature was discovered, but could not be verified. JWTs
     *                                  that fail signature validation should not be trusted and should be discarded.
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an
     *                                  expiration time before the time this method is invoked.
     * @throws IllegalArgumentException if the specified string is {@code null} or empty or only
     *                                  whitespace.
     */
    public String getUsernameFromToken(String token) {
        log.debug("retrieving username from token");
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Parse token and return the expiration date
     *
     * @param token the token
     * @return the token expiration date
     * @throws MalformedJwtException    if the specified JWT was incorrectly constructed (and therefore
     *                                  invalid). Invalid JWTs should not be trusted and should be discarded.
     * @throws SignatureException       if a JWS signature was discovered, but could not be verified. JWTs
     *                                  that fail signature validation should not be trusted and should be discarded.
     * @throws ExpiredJwtException      if the specified JWT is a Claims JWT and the Claims has an
     *                                  expiration time before the time this method is invoked.
     * @throws IllegalArgumentException if the specified string is {@code null} or empty or only
     *                                  whitespace.
     */
    public Date getExpirationDateFromToken(String token) {
        log.debug("retrieving expiration date from token");
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Generates a new token
     *
     * @param userDetails the user detail
     * @return a new token
     */
    public String generateToken(UserDetails userDetails) {
        log.debug("generating token for user [{}]", userDetails.getUsername());
        return doGenerateToken(claimsExtractorChain.getClaims(userDetails), userDetails.getUsername());
    }

    /**
     * Reconstruct user from token
     *
     * @param token the token
     * @return the reconstructed user
     */
    public UserDetails getUserFromToken(String token) {
        log.debug("retrieving user from token");
        Claims claims = getAllClaimsFromToken(token);
        FjwtAbstractUserDetailsBuilder builder = userDetailsBuilderFactory.apply(claims.getSubject());
        claimsExtractorChain.addData(claims, builder);
        UserDetails userDetails = builder.build();
        log.debug("user [{}] retrieved from token", userDetails.getUsername());
        return userDetails;
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaimsFromToken(token));
    }

    private Claims getAllClaimsFromToken(String token) {
        log.debug("parsing token");
        return Jwts.parser()
                .decryptWith(key)
                .verifyWith(key)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = current();
        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(DateUtils.addSeconds(now, fjwtConfig.getTtl()))
                .signWith(key)
                .compact();
    }

    private Date current() {
        return Date.from(
                LocalDateTime.now(clock)
                        .atZone(
                                StringUtils.isNotBlank(fjwtConfig.getZoneId())
                                        ? ZoneId.of(fjwtConfig.getZoneId())
                                        : ZoneId.systemDefault())
                        .toInstant());
    }

    private String toJcaName(String id) {
        return switch (id) {
            case "HS256" -> "HmacSHA256";
            case "HS384" -> "HmacSHA384";
            case "HS512" -> "HmacSHA512";
            case "RS256" -> "SHA256withRSA";
            case "RS384" -> "SHA384withRSA";
            case "RS512" -> "SHA512withRSA";
            case "PS256", "PS384", "PS512" -> "RSASSA-PSS";
            case "ES256" -> "SHA256withECDSA";
            case "ES384" -> "SHA384withECDSA";
            case "ES512" -> "SHA512withECDSA";
            case "EdDSA" -> "EdDSA";
            default ->
                    throw new IllegalStateException("algorithm must be one of  HS256, HS384, HS512, RS256, RS384, RS512, PS256, PS384, PS512, ES256, ES384, ES512, EdDSA");
        };
    }
}
