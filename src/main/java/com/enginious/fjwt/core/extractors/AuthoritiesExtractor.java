package com.enginious.fjwt.core.extractors;

import com.enginious.fjwt.core.ClaimsExtractor;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/** Authorities extractor, adds all authorities to the token */
public class AuthoritiesExtractor extends ClaimsExtractor {

  /** Authorities key */
  public static final String AUTHORITIES = "authorities";

  /** {@inheritDoc} */
  @Override
  public void getClaims(UserDetails source, Map<String, Object> dest) {

    dest.put(
        AUTHORITIES,
        (CollectionUtils.isEmpty(source.getAuthorities())
                ? new ArrayList<>()
                : source.getAuthorities())
            .stream().map(o -> ((GrantedAuthority) o).getAuthority()).collect(Collectors.toList()));
  }
}
