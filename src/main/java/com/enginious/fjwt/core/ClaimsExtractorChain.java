package com.enginious.fjwt.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/**
 * Represents the extractors chain. When the token is generated all registered extractors will be
 * invoked by adding information to the token.
 */
@RequiredArgsConstructor
public class ClaimsExtractorChain {

  /** Extractors chain * */
  private final List<ClaimsExtractor> claimsExtractors;

  /**
   * Invokes the extractors chain adding information to the token and returns a map containing all
   * the extracted information
   *
   * @param source source object
   * @return a map containing all * the extracted information
   */
  public Map<String, Object> getClaims(UserDetails source) {
    Map<String, Object> claims = new HashMap<>();
    if (!CollectionUtils.isEmpty(claimsExtractors)) {
      claimsExtractors.forEach(ce -> ce.getClaims(source, claims));
    }
    return claims;
  }
}
