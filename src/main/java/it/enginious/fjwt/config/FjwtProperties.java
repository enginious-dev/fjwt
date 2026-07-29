package it.enginious.fjwt.config;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import it.enginious.fjwt.core.FjwtClaimsExtractor;

/**
 * Fjwt configuration parameters.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fjwt")
public class FjwtProperties {

  /** User source */
  private UserSourceMode userSource = UserSourceMode.DAO;

  /** Security mode */
  private SecurityMode securityMode = SecurityMode.LEGACY_JWT;

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
  private String algorithm;

  /** Default {@link FjwtClaimsExtractor} enabling flag */
  private boolean enableDefaultExtractors = true;

  /** Frame options */
  private FrameOptions frameOptions = new FrameOptions();

  /** Ldap config properties */
  private LdapConfig ldap = new LdapConfig();

  /** OAuth 2.0 config properties */
  private OAuth2Config oauth2 = new OAuth2Config();

  /**
   * Get all unsecured endpoints (which means this.endpoint + this.unsecured)
   *
   * @return all unsecured endpoints
   */
  public String[] getAllUnsecuredEndpoints() {
    if (!getUnsecured().isEmpty()) {
      for (String endpoint : getUnsecured()) {
        log.info("excluding {} from secured endpoint", endpoint);
      }
    }
    return Stream.concat(Arrays.stream(new String[] {getEndpoint()}), getUnsecured().stream())
        .toArray(String[]::new);
  }

  @Getter
  @Setter
  public static class FrameOptions {
    private boolean disabled = true;
    private boolean sameOrigin = false;
  }

  @Getter
  @Setter
  public static class LdapConfig {
    private String[] urls;
    private String base;
    private String username;
    private String password;
    private String userSearchBase;
    private String userSearchFilter;
    private String groupSearchBase;
    private String groupSearchFilter;
  }

  /** OAuth 2.0 and OpenID Connect integration settings. */
  @Getter
  @Setter
  public static class OAuth2Config {
    /** Enable OpenID Connect endpoints when acting as an authorization server */
    private boolean oidcEnabled = true;

    /** Claim containing application authorities */
    private String authoritiesClaim = "authorities";

    /** Prefix added to authorities read from {@link #authoritiesClaim} */
    private String authorityPrefix = "";

    /** Claim used as authenticated principal name */
    private String principalClaim = "sub";
  }

  /** Supported Fjwt security roles. */
  public enum SecurityMode {
    /** Proprietary username/password endpoint and locally generated JWT */
    LEGACY_JWT,
    /** API protected by access tokens issued by an authorization server */
    OAUTH2_RESOURCE_SERVER,
    /** OAuth 2.0 Authorization Server with optional OpenID Connect */
    OAUTH2_AUTHORIZATION_SERVER
  }

  public enum UserSourceMode {
    DAO,
    LDAP
  }
}
