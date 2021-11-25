package com.enginious.fjwt.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/**
 * Represents the extractors chain. When the token is generated all registered extractors will be
 * invoked by adding information to the token.
 *
 * @since 1.1.0
 * @author Giuseppe Milazzo
 */
@RequiredArgsConstructor
public class FjwtClaimsExtractorChain {

  /** Extractors chain * */
  private final List<FjwtClaimsExtractor> fjwtClaimsExtractors;

  /**
   * Invokes the extractors chain adding information to the token and returns a map containing all
   * the extracted information
   *
   * @param source source object
   * @return a map containing all * the extracted information
   */
  public Claims getClaims(UserDetails source) {
    Claims claims = new DefaultClaims();
    if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
      fjwtClaimsExtractors.forEach(ce -> ce.getClaims(source, claims));
    }
    return claims;
  }

  /**
   * Invokes the extractors chain adding information to the user
   *
   * @param source source map
   * @param dest destination user
   */
  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {
    if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
      fjwtClaimsExtractors.forEach(ce -> ce.addData(source, dest));
    }
  }
}
