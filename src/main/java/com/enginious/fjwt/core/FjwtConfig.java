package com.enginious.fjwt.core;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Fjwt configuration parameters
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fjwt")
public class FjwtConfig {

    /**
     * Jwt authentication endpoint
     */
    private String endpoint = "/authenticate";

    /**
     * List of paths that do not need authentication
     */
    private List<String> unsecured = new ArrayList<>();

    /**
     * Jwt token ttl in seconds
     */
    private int ttl = 3600;

    /**
     * Server secret
     */
    private String secret = "secret";

    /**
     * Server timezone, if blank {@link ZoneId#systemDefault()} will be used
     */
    private String zoneId;

    /**
     * Signature algorithm
     */
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HS512;
}
