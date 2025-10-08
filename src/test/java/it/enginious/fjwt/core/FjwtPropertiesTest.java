package it.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import it.enginious.fjwt.config.FjwtProperties;

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

    assertThat(target.getEndpoint()).isEqualTo("/endpoint");
    assertThat(target.getUnsecured()).isEqualTo(Arrays.asList("/unsecured1", "/unsecured2"));
    assertThat(target.getTtl()).isEqualTo(1);
    assertThat(target.getSecret()).isEqualTo("secret");
    assertThat(target.getZoneId()).isEqualTo("XXX");
    assertThat(target.getAlgorithm()).isEqualTo("HS256");
  }
}
