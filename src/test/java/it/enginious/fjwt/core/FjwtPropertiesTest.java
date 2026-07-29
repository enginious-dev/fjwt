package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import it.enginious.fjwt.config.FjwtProperties;
import it.enginious.fjwt.config.FjwtProperties.SecurityMode;

class FjwtPropertiesTest {

  @Test
  void testAccessor() {

    FjwtProperties target = new FjwtProperties();

    target.setEndpoint("/endpoint");
    target.setUnsecured(Arrays.asList("/unsecured1", "/unsecured2"));
    target.setTtl(1);
    target.setSecret("secret");
    target.setZoneId("XXX");
    target.setAlgorithm("HS256");
    target.setSecurityMode(SecurityMode.OAUTH2_RESOURCE_SERVER);
    target.getOauth2().setAuthoritiesClaim("roles");
    target.getOauth2().setAuthorityPrefix("ROLE_");
    target.getOauth2().setPrincipalClaim("preferred_username");
    target.getOauth2().setOidcEnabled(false);

    assertThat(target.getEndpoint()).isEqualTo("/endpoint");
    assertThat(target.getUnsecured()).isEqualTo(Arrays.asList("/unsecured1", "/unsecured2"));
    assertThat(target.getTtl()).isEqualTo(1);
    assertThat(target.getSecret()).isEqualTo("secret");
    assertThat(target.getZoneId()).isEqualTo("XXX");
    assertThat(target.getAlgorithm()).isEqualTo("HS256");
    assertThat(target.getSecurityMode()).isEqualTo(SecurityMode.OAUTH2_RESOURCE_SERVER);
    assertThat(target.getOauth2().getAuthoritiesClaim()).isEqualTo("roles");
    assertThat(target.getOauth2().getAuthorityPrefix()).isEqualTo("ROLE_");
    assertThat(target.getOauth2().getPrincipalClaim()).isEqualTo("preferred_username");
    assertThat(target.getOauth2().isOidcEnabled()).isFalse();
  }
}
