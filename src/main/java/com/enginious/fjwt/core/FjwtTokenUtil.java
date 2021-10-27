package com.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Jwt token utilities
 */
@Component
@RequiredArgsConstructor
public class FjwtTokenUtil implements Serializable {

    private final FjwtConfig fjwtConfig;

    /**
     * Parse token and return the username
     *
     * @param token the token
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Parse token and return the expiration date
     *
     * @param token the token
     * @return the token expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Validate a token
     *
     * @param token       the token
     * @param userDetails the user detail
     * @return true if the token has not expired and is congruent with the user provided, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        return (getUsernameFromToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Generates a new token
     *
     * @param userDetails the user detail
     * @return a new token
     */
    public String generateToken(UserDetails userDetails) {
        return doGenerateToken(new HashMap<>(), userDetails.getUsername());
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(getAllClaimsFromToken(token));
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(fjwtConfig.getSecret()).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(DateUtils.addSeconds(new Date(), fjwtConfig.getTtl()))
                .signWith(fjwtConfig.getAlgorithm(), fjwtConfig.getSecret())
                .compact();
    }
}