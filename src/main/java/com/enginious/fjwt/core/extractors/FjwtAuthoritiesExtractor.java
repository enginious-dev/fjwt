package com.enginious.fjwt.core.extractors;

import com.enginious.fjwt.core.FjwtAbstractUserDetailsBuilder;
import com.enginious.fjwt.core.FjwtClaimsExtractor;
import io.jsonwebtoken.Claims;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/**
 * Authorities extractor, adds all authorities to the token.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
public class FjwtAuthoritiesExtractor implements FjwtClaimsExtractor {

  /** Authorities key */
  public static final String AUTHORITIES = "authorities";

  /** {@inheritDoc} */
  @Override
  public void getClaims(UserDetails source, Claims dest) {

    dest.put(
        AUTHORITIES,
        (CollectionUtils.isEmpty(source.getAuthorities())
                ? new ArrayList<>()
                : source.getAuthorities())
            .stream().map(o -> ((GrantedAuthority) o).getAuthority()).collect(Collectors.toList()));
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
    Collection<String> authorities =
        ObjectUtils.defaultIfNull(
            source.get(AUTHORITIES, Collection.class), new ArrayList<String>());
    if (!CollectionUtils.isEmpty(authorities)) {
      dest.authorities(
          authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
    }
  }
}
