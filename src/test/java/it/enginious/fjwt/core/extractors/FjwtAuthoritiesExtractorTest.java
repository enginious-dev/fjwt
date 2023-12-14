package it.enginious.fjwt.core.extractors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultClaimsBuilder;
import it.enginious.fjwt.core.FjwtSimpleUserDetailsBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class FjwtAuthoritiesExtractorTest {

    private final FjwtAuthoritiesExtractor target = new FjwtAuthoritiesExtractor();

    @Test
    @SuppressWarnings("unchecked")
    void whenGetClaimsAndSourceAuthoritiesIsEmptyMapShouldContainsNoAuthorities() {
        ClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        target.getClaims(
                User.builder()
                        .username("test")
                        .password("test")
                        .authorities(new GrantedAuthority[]{})
                        .build(),
                claimsBuilder);
        Claims claims = claimsBuilder.build();
        assertThat(claims).hasSize(1);
        assertThat(claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isNotNull();
        assertThat((Collection<String>) claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenGetClaimsAndSourceAuthoritiesContainsSomethingMapShouldContainsSameAuthorities() {
        ClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        target.getClaims(
                User.builder()
                        .username("test")
                        .password("test")
                        .authorities(
                                new GrantedAuthority[]{
                                        new SimpleGrantedAuthority("auth1"), new SimpleGrantedAuthority("auth2")
                                })
                        .build(),
                claimsBuilder);
        Claims claims = claimsBuilder.build();
        assertThat(claims).hasSize(1);
        assertThat(claims.get(FjwtAuthoritiesExtractor.AUTHORITIES)).isNotNull();
        Collection<String> authorities =
                (Collection<String>) claims.get(FjwtAuthoritiesExtractor.AUTHORITIES);
        assertThat(authorities).hasSize(2).contains("auth1", "auth2");
    }

    @Test
    void whenAddDataAndSourceIsEmptyThenUserShouldNotHaveAuthorities() {
        FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
        target.addData(new DefaultClaims(new HashMap<>()), builder);
        UserDetails userDetails = builder.build();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void whenAddDataAndSourceHaveNoAuthoritiesThenUserShouldNotHaveAuthorities() {
        DefaultClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        claimsBuilder.add("dummy", new Object());
        FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
        target.addData(claimsBuilder.build(), builder);
        UserDetails userDetails = builder.build();
        assertThat(userDetails.getAuthorities()).isEmpty();
    }

    @Test
    void whenAddDataAndSourceHaveSomeAuthoritiesThenUserShouldHaveSameAuthorities() {
        DefaultClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();
        claimsBuilder.add(FjwtAuthoritiesExtractor.AUTHORITIES, Collections.singletonList("auth1"));
        FjwtSimpleUserDetailsBuilder builder = new FjwtSimpleUserDetailsBuilder("user");
        target.addData(claimsBuilder.build(), builder);
        UserDetails userDetails = builder.build();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("auth1");
    }
}
