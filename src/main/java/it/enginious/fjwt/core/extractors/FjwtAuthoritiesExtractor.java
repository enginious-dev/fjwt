package it.enginious.fjwt.core.extractors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import it.enginious.fjwt.core.FjwtAbstractUserDetailsBuilder;
import it.enginious.fjwt.core.FjwtClaimsExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Authorities extractor, adds all authorities to the token.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
@Slf4j
public class FjwtAuthoritiesExtractor implements FjwtClaimsExtractor {

    /**
     * Authorities key
     */
    public static final String AUTHORITIES = "authorities";

    /**
     * {@inheritDoc}
     */
    @Override
    public void getClaims(UserDetails source, ClaimsBuilder dest) {

        log.debug(
                "found [{}] authorities in user",
                Objects.nonNull(source.getAuthorities()) ? source.getAuthorities().size() : 0);

        dest.add(
                AUTHORITIES,
                (CollectionUtils.isEmpty(source.getAuthorities())
                        ? new ArrayList<>()
                        : source.getAuthorities())
                        .stream()
                        .map(
                                o -> {
                                    String authority = ((GrantedAuthority) o).getAuthority();
                                    log.debug("adding authority with value [{}]", authority);
                                    return authority;
                                })
                        .toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {

        Collection<String> authorities =
                ObjectUtils.defaultIfNull(
                        source.get(AUTHORITIES, Collection.class), new ArrayList<String>());

        log.debug(
                "found [{}] authorities in token", Objects.nonNull(authorities) ? authorities.size() : 0);

        if (!CollectionUtils.isEmpty(authorities)) {
            dest.authorities(
                    authorities.stream()
                            .map(
                                    a -> {
                                        log.debug("retrieved authority with value [{}]", a);
                                        return new SimpleGrantedAuthority(a);
                                    })
                            .toList());
        }
    }
}
