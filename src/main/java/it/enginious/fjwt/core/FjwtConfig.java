package it.enginious.fjwt.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fjwt configuration parameters.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
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
    private String secret;

    /**
     * Server timezone, if blank {@link ZoneId#systemDefault()} will be used
     */
    private String zoneId;

    /**
     * Signature algorithm
     */
    private String algorithm;

    /**
     * Default {@link FjwtClaimsExtractor} enabling flag
     */
    private boolean enableDefaultExtractors = true;

    /**
     * Get all unsecured endpoints (which means this.endpoint + this.unsecured)
     *
     * @return all unsecured endpoints
     */
    public String[] getAllUnsecuredEndpoints() {
        return Stream.concat(Arrays.stream(new String[]{getEndpoint()}), getUnsecured().stream())
                .toArray(String[]::new);
    }
}
