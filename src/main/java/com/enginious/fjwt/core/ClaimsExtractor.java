package com.enginious.fjwt.core;

import java.util.Map;
import org.springframework.security.core.userdetails.UserDetails;

/** An element of the token extraction chain. See {@link ClaimsExtractorChain} */
public abstract class ClaimsExtractor {

  /**
   * Extract fields from source and add them to the dest map
   *
   * @param source source object
   * @param dest target map
   */
  public abstract void getClaims(UserDetails source, Map<String, Object> dest);
}
