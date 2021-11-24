package com.enginious.fjwt.core.extractors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

class AuthoritiesExtractorTest {

  private AuthoritiesExtractor target = new AuthoritiesExtractor();

  @Test
  @SuppressWarnings("unchecked")
  void whenGetClaimsAndSourceAuthoritiesIsEmptyMapShouldContainsNoAuthorities() {
    Map<String, Object> claims = new HashMap<>();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .build(),
        claims);
    Assertions.assertThat(claims).hasSize(1);
    Assertions.assertThat(claims.get(AuthoritiesExtractor.AUTHORITIES)).isNotNull();
    Assertions.assertThat((Collection<String>) claims.get(AuthoritiesExtractor.AUTHORITIES))
        .isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void whenGetClaimsAndSourceAuthoritiesContainsSomethingMapShouldContainsSameAuthorities() {
    Map<String, Object> claims = new HashMap<>();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(
                new GrantedAuthority[] {
                  new SimpleGrantedAuthority("auth1"), new SimpleGrantedAuthority("auth2")
                })
            .build(),
        claims);
    Assertions.assertThat(claims).hasSize(1);
    Assertions.assertThat(claims.get(AuthoritiesExtractor.AUTHORITIES)).isNotNull();
    Collection<String> authorities =
        (Collection<String>) claims.get(AuthoritiesExtractor.AUTHORITIES);
    Assertions.assertThat(authorities).hasSize(2);
    Assertions.assertThat(authorities).contains("auth1");
    Assertions.assertThat(authorities).contains("auth2");
  }
}
