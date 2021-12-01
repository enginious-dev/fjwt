package com.enginious.fjwt.core.extractors;

import static org.assertj.core.api.Assertions.assertThat;

import com.enginious.fjwt.core.FjwtSimpleUserDetailsBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class FjwtAuthoritiesExtractorTest {

  private FjwtAuthoritiesExtractor target = new FjwtAuthoritiesExtractor();

  @Test
  @SuppressWarnings("unchecked")
  void whenGetClaimsAndSourceAuthoritiesIsEmptyMapShouldContainsNoAuthorities() {
    Claims claims = new DefaultClaims();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .build(),
        claims);
    assertThat(claims).hasSize(1);
    assertThat(claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isNotNull();
    assertThat((Collection<String>) claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void whenGetClaimsAndSourceAuthoritiesContainsSomethingMapShouldContainsSameAuthorities() {
    Claims claims = new DefaultClaims();
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
    assertThat(claims).hasSize(1);
    assertThat(claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isNotNull();
    Collection<String> authorities =
        (Collection<String>) claims.get(FjwtAuthoritiesExtractor.AUTHORITIES);
    assertThat(authorities).hasSize(2).contains("auth1", "auth2");
  }

  @Test
  void whenAddDataAndSourceIsEmptyThenUserShouldNotHaveAuthorities() {
    FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
    target.addData(new DefaultClaims(), builder);
    UserDetails userDetails = builder.build();
    assertThat(userDetails.getAuthorities()).isEmpty();
  }

  @Test
  void whenAddDataAndSourceHaveNoAuthoritiesThenUserShouldNotHaveAuthorities() {
    DefaultClaims claims = new DefaultClaims();
    claims.put("dummy", new Object());
    FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
    target.addData(claims, builder);
    UserDetails userDetails = builder.build();
    assertThat(userDetails.getAuthorities()).isEmpty();
  }

  @Test
  void whenAddDataAndSourceHaveSomeAuthoritiesThenUserShouldHaveSameAuthorities() {
    DefaultClaims claims = new DefaultClaims();
    claims.put(FjwtAuthoritiesExtractor.AUTHORITIES, Collections.singletonList("auth1"));
    FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
    target.addData(claims, builder);
    UserDetails userDetails = builder.build();
    assertThat(userDetails.getAuthorities()).hasSize(1);
    assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("auth1");
  }
}
