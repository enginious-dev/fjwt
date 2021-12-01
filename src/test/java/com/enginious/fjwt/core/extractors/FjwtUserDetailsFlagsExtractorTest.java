package com.enginious.fjwt.core.extractors;

import static org.assertj.core.api.Assertions.assertThat;

import com.enginious.fjwt.core.FjwtSimpleUserDetailsBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class FjwtUserDetailsFlagsExtractorTest {

  private FjwtUserDetailsFlagsExtractor target = new FjwtUserDetailsFlagsExtractor();

  @Test
  void whenGetClaimsMapShouldContainsAllFlag() {
    Claims claims = new DefaultClaims();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .build(),
        claims);
    assertThat(claims).hasSizeGreaterThanOrEqualTo(4);
    assertThat(claims.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isNotNull();
    assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isNotNull();
    assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isNotNull();
    assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isNotNull();
  }

  @Test
  void whenGetClaimsMapShouldContainsAllFlagInverted() {
    Claims claims1 = new DefaultClaims();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .build(),
        claims1);
    assertThat(claims1).hasSizeGreaterThanOrEqualTo(4);
    assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isFalse();
    assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isFalse();
    assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isFalse();
    assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isTrue();

    Claims claims2 = new DefaultClaims();
    target.getClaims(
        User.builder()
            .username("test")
            .password("test")
            .authorities(new GrantedAuthority[] {})
            .disabled(true)
            .accountLocked(true)
            .accountExpired(true)
            .credentialsExpired(true)
            .build(),
        claims2);
    assertThat(claims2).hasSizeGreaterThanOrEqualTo(4);
    assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isTrue();
    assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isTrue();
    assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isTrue();
    assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isFalse();
  }

  @Test
  void whenAddDataUserShouldContainsAllFlag() {
    Claims claims = new DefaultClaims();
    claims.put(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED, true);
    claims.put(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED, true);
    claims.put(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED, true);
    claims.put(FjwtUserDetailsFlagsExtractor.ENABLED, false);
    FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("username");
    target.addData(claims, builder);
    UserDetails userDetails = builder.build();
    assertThat(userDetails.isCredentialsNonExpired()).isFalse();
    assertThat(userDetails.isAccountNonExpired()).isFalse();
    assertThat(userDetails.isAccountNonLocked()).isFalse();
    assertThat(userDetails.isEnabled()).isFalse();
  }
}
