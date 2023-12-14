package it.enginious.fjwt.core;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtConfigTest {

    @Test
    void testAccessor() {

        FjwtConfig target = new FjwtConfig();

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
