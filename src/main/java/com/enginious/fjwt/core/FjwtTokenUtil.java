package com.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Jwt token utilities.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FjwtTokenUtil {

  /** The clock */
  private final Clock clock;

  /** Fjwt configuration */
  private final FjwtConfig fjwtConfig;

  /** Claims extractor chain */
  private final FjwtClaimsExtractorChain claimsExtractorChain;

  /** User details builder factory */
  private final FjwtUserDetailsBuilderFactory userDetailsBuilderFactory;

  /**
   * Parse token and return the username
   *
   * @param token the token
   * @return the username
   * @throws MalformedJwtException if the specified JWT was incorrectly constructed (and therefore
   *     invalid). Invalid JWTs should not be trusted and should be discarded.
   * @throws SignatureException if a JWS signature was discovered, but could not be verified. JWTs
   *     that fail signature validation should not be trusted and should be discarded.
   * @throws ExpiredJwtException if the specified JWT is a Claims JWT and the Claims has an
   *     expiration time before the time this method is invoked.
   * @throws IllegalArgumentException if the specified string is {@code null} or empty or only
   *     whitespace.
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
   * @throws MalformedJwtException if the specified JWT was incorrectly constructed (and therefore
   *     invalid). Invalid JWTs should not be trusted and should be discarded.
   * @throws SignatureException if a JWS signature was discovered, but could not be verified. JWTs
   *     that fail signature validation should not be trusted and should be discarded.
   * @throws ExpiredJwtException if the specified JWT is a Claims JWT and the Claims has an
   *     expiration time before the time this method is invoked.
   * @throws IllegalArgumentException if the specified string is {@code null} or empty or only
   *     whitespace.
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
        .setSigningKey(
            new SecretKeySpec(
                fjwtConfig.getSecret().getBytes(StandardCharsets.UTF_8),
                fjwtConfig.getAlgorithm().getJcaName()))
        .parseClaimsJws(token)
        .getBody();
  }

  private String doGenerateToken(Map<String, Object> claims, String subject) {
    Date now = current();
    return Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(now)
        .setExpiration(DateUtils.addSeconds(now, fjwtConfig.getTtl()))
        .signWith(
            fjwtConfig.getAlgorithm(),
            new SecretKeySpec(
                fjwtConfig.getSecret().getBytes(StandardCharsets.UTF_8),
                fjwtConfig.getAlgorithm().getJcaName()))
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
}
