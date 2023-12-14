package it.enginious.fjwt.core.extractors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.impl.DefaultClaimsBuilder;
import it.enginious.fjwt.core.FjwtSimpleUserDetailsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtUserDetailsFlagsExtractorTest {

    private final FjwtUserDetailsFlagsExtractor target = new FjwtUserDetailsFlagsExtractor();

    @Test
    void whenGetClaimsMapShouldContainsAllFlag() {
        ClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        target.getClaims(
                User.builder()
                        .username("test")
                        .password("test")
                        .authorities(new GrantedAuthority[]{})
                        .build(),
                claimsBuilder);
        Claims claims = claimsBuilder.build();
        assertThat(claims).hasSizeGreaterThanOrEqualTo(4);
        assertThat(claims.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isNotNull();
        assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isNotNull();
        assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isNotNull();
        assertThat(claims.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isNotNull();
    }

    @Test
    void whenGetClaimsMapShouldContainsAllFlagInverted() {
        ClaimsBuilder claimsBuilder1 = new DefaultClaimsBuilder();
        target.getClaims(
                User.builder()
                        .username("test")
                        .password("test")
                        .authorities(new GrantedAuthority[]{})
                        .build(),
                claimsBuilder1);
        Claims claims1 = claimsBuilder1.build();
        assertThat(claims1).hasSizeGreaterThanOrEqualTo(4);
        assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isFalse();
        assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isFalse();
        assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isFalse();
        assertThat((Boolean) claims1.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isTrue();

        ClaimsBuilder claimsBuilder2 = new DefaultClaimsBuilder();
        target.getClaims(
                User.builder()
                        .username("test")
                        .password("test")
                        .authorities(new GrantedAuthority[]{})
                        .disabled(true)
                        .accountLocked(true)
                        .accountExpired(true)
                        .credentialsExpired(true)
                        .build(),
                claimsBuilder2);
        Claims claims2 = claimsBuilder2.build();
        assertThat(claims2).hasSizeGreaterThanOrEqualTo(4);
        assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED)).isTrue();
        assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED)).isTrue();
        assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED)).isTrue();
        assertThat((Boolean) claims2.get(FjwtUserDetailsFlagsExtractor.ENABLED)).isFalse();
    }

    @Test
    void whenAddDataUserShouldContainsAllFlag() {
        DefaultClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        claimsBuilder.put(FjwtUserDetailsFlagsExtractor.CREDENTIALS_EXPIRED, true);
        claimsBuilder.put(FjwtUserDetailsFlagsExtractor.ACCOUNT_EXPIRED, true);
        claimsBuilder.put(FjwtUserDetailsFlagsExtractor.ACCOUNT_LOCKED, true);
        claimsBuilder.put(FjwtUserDetailsFlagsExtractor.ENABLED, false);
        FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("username");
        target.addData(claimsBuilder.build(), builder);
        UserDetails userDetails = builder.build();
        assertThat(userDetails.isCredentialsNonExpired()).isFalse();
        assertThat(userDetails.isAccountNonExpired()).isFalse();
        assertThat(userDetails.isAccountNonLocked()).isFalse();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}
