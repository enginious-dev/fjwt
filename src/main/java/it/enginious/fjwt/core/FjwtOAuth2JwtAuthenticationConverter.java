package it.enginious.fjwt.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import it.enginious.fjwt.config.FjwtProperties;

/**
 * Converts a validated OAuth 2.0 JWT into an authentication, retaining standard scopes and Fjwt
 * application authorities.
 *
 * @author Giuseppe Milazzo
 * @since 4.2.0
 */
public class FjwtOAuth2JwtAuthenticationConverter
    implements Converter<Jwt, JwtAuthenticationToken> {

  private final JwtGrantedAuthoritiesConverter scopesConverter =
      new JwtGrantedAuthoritiesConverter();
  private final FjwtProperties fjwtProperties;

  /**
   * Builds a converter using Fjwt OAuth 2.0 claim mapping properties.
   *
   * @param fjwtProperties Fjwt properties
   */
  public FjwtOAuth2JwtAuthenticationConverter(FjwtProperties fjwtProperties) {
    this.fjwtProperties = fjwtProperties;
  }

  @Override
  public JwtAuthenticationToken convert(Jwt jwt) {
    Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopesConverter.convert(jwt));
    authorities.addAll(applicationAuthorities(jwt));

    String principalClaim = fjwtProperties.getOauth2().getPrincipalClaim();
    String configuredPrincipal =
        StringUtils.isBlank(principalClaim)
            ? null
            : Objects.toString(jwt.getClaim(principalClaim), null);
    String principalName = StringUtils.defaultIfBlank(configuredPrincipal, jwt.getSubject());

    return new JwtAuthenticationToken(jwt, authorities, principalName);
  }

  private Collection<GrantedAuthority> applicationAuthorities(Jwt jwt) {
    String authoritiesClaim = fjwtProperties.getOauth2().getAuthoritiesClaim();
    if (StringUtils.isBlank(authoritiesClaim)) {
      return Set.of();
    }

    Object claim = jwt.getClaim(authoritiesClaim);
    Collection<?> values =
        switch (claim) {
          case null -> Set.of();
          case Collection<?> collection -> collection;
          default ->
              Arrays.stream(claim.toString().split("[,\\s]+"))
                  .filter(StringUtils::isNotBlank)
                  .toList();
        };

    String prefix = StringUtils.defaultString(fjwtProperties.getOauth2().getAuthorityPrefix());
    return values.stream()
        .filter(Objects::nonNull)
        .map(Object::toString)
        .filter(StringUtils::isNotBlank)
        .<GrantedAuthority>map(authority -> new SimpleGrantedAuthority(prefix + authority))
        .toList();
  }
}
