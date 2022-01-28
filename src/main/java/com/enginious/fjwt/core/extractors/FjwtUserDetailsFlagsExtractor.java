package com.enginious.fjwt.core.extractors;

import com.enginious.fjwt.core.FjwtAbstractUserDetailsBuilder;
import com.enginious.fjwt.core.FjwtClaimsExtractor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * UserDetails flags extractor, adds all flag in {@link UserDetails} to the token.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
@Slf4j
public class FjwtUserDetailsFlagsExtractor implements FjwtClaimsExtractor {

  private static final String ADDING_VALUE_PATTERN = "adding [{}] with value [{}]";
  private static final String RETRIEVED_VALUE_PATTERN = "retrieved [{}] with value [{}]";

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

    log.debug(ADDING_VALUE_PATTERN, CREDENTIALS_EXPIRED, !source.isCredentialsNonExpired());
    dest.put(CREDENTIALS_EXPIRED, !source.isCredentialsNonExpired());

    log.debug(ADDING_VALUE_PATTERN, ACCOUNT_EXPIRED, !source.isAccountNonExpired());
    dest.put(ACCOUNT_EXPIRED, !source.isAccountNonExpired());

    log.debug(ADDING_VALUE_PATTERN, ACCOUNT_LOCKED, !source.isAccountNonLocked());
    dest.put(ACCOUNT_LOCKED, !source.isAccountNonLocked());

    log.debug(ADDING_VALUE_PATTERN, ENABLED, source.isEnabled());
    dest.put(ENABLED, source.isEnabled());
  }

  /** {@inheritDoc} */
  @Override
  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {

    log.debug(
        RETRIEVED_VALUE_PATTERN,
        CREDENTIALS_EXPIRED,
        source.get(CREDENTIALS_EXPIRED, Boolean.class));
    dest.credentialsExpired(source.get(CREDENTIALS_EXPIRED, Boolean.class));

    log.debug(RETRIEVED_VALUE_PATTERN, ACCOUNT_EXPIRED, source.get(ACCOUNT_EXPIRED, Boolean.class));
    dest.accountExpired(source.get(ACCOUNT_EXPIRED, Boolean.class));

    log.debug(RETRIEVED_VALUE_PATTERN, ACCOUNT_LOCKED, source.get(ACCOUNT_LOCKED, Boolean.class));
    dest.accountLocked(source.get(ACCOUNT_LOCKED, Boolean.class));

    log.debug(RETRIEVED_VALUE_PATTERN, ENABLED, source.get(ENABLED, Boolean.class));
    dest.enabled(source.get(ENABLED, Boolean.class));
  }
}
