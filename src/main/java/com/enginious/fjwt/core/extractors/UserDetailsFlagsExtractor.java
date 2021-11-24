package com.enginious.fjwt.core.extractors;

import com.enginious.fjwt.core.ClaimsExtractor;
import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

/** UserDetails flags extractor, adds all flag in {@link UserDetails} to the token */
public class UserDetailsFlagsExtractor extends ClaimsExtractor {

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
  public void getClaims(UserDetails source, Map<String, Object> dest) {

    dest.put(CREDENTIALS_EXPIRED, !source.isCredentialsNonExpired());
    dest.put(ACCOUNT_EXPIRED, !source.isAccountNonExpired());
    dest.put(ACCOUNT_LOCKED, !source.isAccountNonLocked());
    dest.put(ENABLED, source.isEnabled());
  }
}
