package com.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * An element of the token extraction chain. See {@link FjwtClaimsExtractorChain}.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
public interface FjwtClaimsExtractor {

  /**
   * Extract fields from source and add them to the dest map
   *
   * @param source source object
   * @param dest target map
   */
  void getClaims(UserDetails source, Claims dest);

  /**
   * Extract fields from source and add them into user
   *
   * @param source source object
   * @param dest target user builder
   */
  void addData(Claims source, FjwtAbstractUserDetailsBuilder dest);
}
