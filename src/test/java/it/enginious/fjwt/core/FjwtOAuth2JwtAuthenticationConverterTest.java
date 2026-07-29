package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import it.enginious.fjwt.config.FjwtProperties;

class FjwtOAuth2JwtAuthenticationConverterTest {

  @Test
  void shouldMergeScopesAndApplicationAuthorities() {
    FjwtProperties properties = new FjwtProperties();
    properties.getOauth2().setAuthoritiesClaim("roles");
    properties.getOauth2().setAuthorityPrefix("ROLE_");
    properties.getOauth2().setPrincipalClaim("preferred_username");

    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("subject")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim("scope", "api.read api.write")
            .claim("roles", List.of("ADMIN", "AUDITOR"))
            .claim("preferred_username", "alice")
            .build();

    var authentication = new FjwtOAuth2JwtAuthenticationConverter(properties).convert(jwt);

    assertThat(authentication.getName()).isEqualTo("alice");
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactlyInAnyOrder(
            "SCOPE_api.read", "SCOPE_api.write", "ROLE_ADMIN", "ROLE_AUDITOR");
  }

  @Test
  void shouldUseSubjectAndAcceptDelimitedAuthorities() {
    FjwtProperties properties = new FjwtProperties();

    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .subject("alice")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .claim("authorities", "ROLE_ADMIN, ROLE_USER")
            .build();

    var authentication = new FjwtOAuth2JwtAuthenticationConverter(properties).convert(jwt);

    assertThat(authentication.getName()).isEqualTo("alice");
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
  }
}
