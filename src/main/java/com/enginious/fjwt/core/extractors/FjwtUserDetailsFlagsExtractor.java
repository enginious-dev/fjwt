package com.enginious.fjwt.core.extractors;

import com.enginious.fjwt.core.FjwtAbstractUserDetailsBuilder;
import com.enginious.fjwt.core.FjwtClaimsExtractor;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

/** UserDetails flags extractor, adds all flag in {@link UserDetails} to the token */
public class FjwtUserDetailsFlagsExtractor implements FjwtClaimsExtractor {

  /** Credential credentials expired flag key */
  public static final String CREDENTIALS_EXPIRED = "credentialsExpired";
  /** Account Expired flag key */
  public static final String ACCOUNT_EXPIRED = "accountExpired";
  /** Account Locked flag key */
  public static final String ACCOUNT_LOCKED = "accountLocked";
  /** Enabled flag key */
  public static final String ENABLED = "enabled";

  /** {@inheritDoc} */
  @Override
  public void getClaims(UserDetails source, Claims dest) {

    dest.put(CREDENTIALS_EXPIRED, !source.isCredentialsNonExpired());
    dest.put(ACCOUNT_EXPIRED, !source.isAccountNonExpired());
    dest.put(ACCOUNT_LOCKED, !source.isAccountNonLocked());
    dest.put(ENABLED, source.isEnabled());
  }

  /** {@inheritDoc} */
  @Override
  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
    dest.credentialsExpired(source.get(CREDENTIALS_EXPIRED, Boolean.class));
    dest.accountExpired(source.get(ACCOUNT_EXPIRED, Boolean.class));
    dest.accountLocked(source.get(ACCOUNT_LOCKED, Boolean.class));
    dest.enabled(source.get(ENABLED, Boolean.class));
  }
}
