package com.enginious.fjwt.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FjwtResponseTest {

  @Test
  void testAccessor() {

    FjwtResponse rs = FjwtResponse.builder().token("token1").build();

    assertThat(rs.getToken()).isEqualTo("token1");

    rs.setToken("token2");

    assertThat(rs.getToken()).isEqualTo("token2");
  }
}
