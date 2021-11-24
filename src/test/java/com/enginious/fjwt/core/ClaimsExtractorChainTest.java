package com.enginious.fjwt.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class ClaimsExtractorChainTest {

  @Test
  void whenGetClaimsWithEmptyChainShouldReturnEmptyMap() {
    ClaimsExtractorChain target = new ClaimsExtractorChain(null);
    Assertions.assertThat(target.getClaims(new User("test", "test", Collections.emptyList())))
        .isEmpty();
  }

  @Test
  void whenGetClaimsWithNotEmptyChainShouldReturnNotEmptyMap() {
    ClaimsExtractorChain target =
        new ClaimsExtractorChain(
            Arrays.asList(
                new ClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Map<String, Object> dest) {
                    dest.put("ce1", source.getUsername());
                  }
                },
                new ClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Map<String, Object> dest) {
                    dest.put("ce2", source.getUsername());
                  }
                }));

    Map<String, Object> claims =
        target.getClaims(new User("test", "test", Collections.emptyList()));
    Assertions.assertThat(claims).hasSize(2);
    Assertions.assertThat(claims).containsKey("ce1");
    Assertions.assertThat(claims).containsKey("ce2");
  }
}
