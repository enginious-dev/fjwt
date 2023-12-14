package it.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FjwtClaimsExtractorChainTest {

    @Mock
    private Claims claims;

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
                                    public void getClaims(UserDetails source, ClaimsBuilder dest) {
                                        dest.add("ce1", source.getUsername());
                                    }

                                    @Override
                                    public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                                        throw new UnsupportedOperationException();
                                    }
                                },
                                new FjwtClaimsExtractor() {

                                    @Override
                                    public void getClaims(UserDetails source, ClaimsBuilder dest) {
                                        dest.add("ce2", source.getUsername());
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
                                    public void getClaims(UserDetails source, ClaimsBuilder dest) {
                                        throw new UnsupportedOperationException();
                                    }

                                    @Override
                                    public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
                                        source.get("c1");
                                    }
                                },
                                new FjwtClaimsExtractor() {

                                    @Override
                                    public void getClaims(UserDetails source, ClaimsBuilder dest) {
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
