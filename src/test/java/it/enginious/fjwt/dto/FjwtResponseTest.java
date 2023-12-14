package it.enginious.fjwt.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtResponseTest {

    @Test
    void testDefaultConstructor() {

        FjwtResponse rs = new FjwtResponse();

        assertThat(rs.getToken()).isBlank();
    }

    @Test
    void testAccessor() {

        FjwtResponse rs = FjwtResponse.builder().token("token1").build();

        assertThat(rs.getToken()).isEqualTo("token1");

        rs.setToken("token2");

        assertThat(rs.getToken()).isEqualTo("token2");
    }
}
