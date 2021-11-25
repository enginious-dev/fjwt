package com.enginious.fjwt.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

import io.jsonwebtoken.Claims;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class FjwtClaimsExtractorChainTest {

  @Mock private Claims claims;

  @Test
  void whenGetClaimsWithEmptyChainShouldReturnEmptyMap() {
    FjwtClaimsExtractorChain target = new FjwtClaimsExtractorChain(null);
    assertThat(target.getClaims(new User("test", "test", Collections.emptyList()))).isEmpty();
  }

  @Test
  void whenGetClaimsWithNotEmptyChainShouldReturnNotEmptyMap() {
    FjwtClaimsExtractorChain target =
        new FjwtClaimsExtractorChain(
            Arrays.asList(
                new FjwtClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Claims dest) {
                    dest.put("ce1", source.getUsername());
                  }

                  @Override
                  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                    throw new UnsupportedOperationException();
                  }
                },
                new FjwtClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Claims dest) {
                    dest.put("ce2", source.getUsername());
                  }

                  @Override
                  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                    throw new UnsupportedOperationException();
                  }
                }));

    Map<String, Object> claims =
        target.getClaims(new User("test", "test", Collections.emptyList()));
    assertThat(claims).hasSize(2).containsKeys("ce1", "ce2");
  }

  @Test
  void whenAddDataWithEmptyChainShouldDoNothing() {
    FjwtClaimsExtractorChain target = new FjwtClaimsExtractorChain(null);
    target.addData(claims, new FjwtSimpleUserDetailsBuilder("username"));
    then(claims).should(never()).get(anyString(), any());
  }

  @Test
  void whenAddDataWithNotEmptyChainShouldCallGetOnClaims() {
    FjwtClaimsExtractorChain target =
        new FjwtClaimsExtractorChain(
            Arrays.asList(
                new FjwtClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Claims dest) {
                    throw new UnsupportedOperationException();
                  }

                  @Override
                  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                    source.get("c1");
                  }
                },
                new FjwtClaimsExtractor() {

                  @Override
                  public void getClaims(UserDetails source, Claims dest) {
                    throw new UnsupportedOperationException();
                  }

                  @Override
                  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                    source.get("c2");
                  }
                }));

    target.addData(claims, new FjwtSimpleUserDetailsBuilder("username"));
    then(claims).should(times(1)).get("c1");
    then(claims).should(times(1)).get("c2");
  }
}
