package com.enginious.fjwt.core;

import io.jsonwebtoken.SignatureAlgorithm;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Fjwt configuration parameters.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fjwt")
public class FjwtConfig {

  /** Jwt authentication endpoint */
  private String endpoint = "/authenticate";

  /** List of paths that do not need authentication */
  private List<String> unsecured = new ArrayList<>();

  /** Jwt token ttl in seconds */
  private int ttl = 3600;

  /** Server secret */
  private String secret;

  /** Server timezone, if blank {@link ZoneId#systemDefault()} will be used */
  private String zoneId;

  /** Signature algorithm */
  private SignatureAlgorithm algorithm = SignatureAlgorithm.HS512;

  /** Default {@link FjwtClaimsExtractor} enabling flag */
  private boolean enableDefaultExtractors = true;
}
