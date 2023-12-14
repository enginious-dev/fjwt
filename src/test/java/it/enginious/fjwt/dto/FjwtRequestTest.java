package it.enginious.fjwt.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtRequestTest {

    @Test
    void testDefaultConstructor() {

        FjwtRequest rq = new FjwtRequest();

        assertThat(rq.getUsername()).isBlank();
        assertThat(rq.getPassword()).isBlank();
    }

    @Test
    void testAccessor() {

        FjwtRequest rq = FjwtRequest.builder().username("username1").password("password1").build();

        assertThat(rq.getUsername()).isEqualTo("username1");
        assertThat(rq.getPassword()).isEqualTo("password1");

        rq.setUsername("username2");
        rq.setPassword("password2");

        assertThat(rq.getUsername()).isEqualTo("username2");
        assertThat(rq.getPassword()).isEqualTo("password2");
    }
}
