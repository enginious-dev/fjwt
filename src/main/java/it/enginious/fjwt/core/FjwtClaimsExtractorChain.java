package it.enginious.fjwt.core;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaimsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

/**
 * Represents the extractors chain. When the token is generated all registered extractors will be
 * invoked by adding information to the token.
 *
 * @author Giuseppe Milazzo
 * @since 1.1.0
 */
@Slf4j
public class FjwtClaimsExtractorChain {

  /** Extractors chain * */
  private final List<FjwtClaimsExtractor> fjwtClaimsExtractors;

  /** Provider-independent contributors chain */
  private final List<FjwtClaimContributor> fjwtClaimContributors;

  /**
   * Builds a chain containing only legacy extractors.
   *
   * @param fjwtClaimsExtractors legacy extractors
   */
  public FjwtClaimsExtractorChain(List<FjwtClaimsExtractor> fjwtClaimsExtractors) {
    this(fjwtClaimsExtractors, List.of());
  }

  /**
   * Builds a chain containing legacy extractors and provider-independent contributors.
   *
   * @param fjwtClaimsExtractors legacy extractors
   * @param fjwtClaimContributors provider-independent contributors
   */
  public FjwtClaimsExtractorChain(
      List<FjwtClaimsExtractor> fjwtClaimsExtractors,
      List<FjwtClaimContributor> fjwtClaimContributors) {
    this.fjwtClaimsExtractors = fjwtClaimsExtractors;
    this.fjwtClaimContributors = fjwtClaimContributors;
  }

  /**
   * Invokes the extractors chain adding information to the token and returns a map containing all
   * the extracted information
   *
   * @param source source object
   * @return a map containing all the extracted information
   */
  public Claims getClaims(UserDetails source) {

    DefaultClaimsBuilder claimsBuilder = new DefaultClaimsBuilder();

    log.debug(
        "found [{}] extractors in chain",
        Objects.nonNull(fjwtClaimsExtractors) ? fjwtClaimsExtractors.size() : 0);

    if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
      fjwtClaimsExtractors.forEach(
          ce -> {
            log.debug("retrieving claims from user using [{}]", ce.getClass().getName());
            ce.getClaims(source, claimsBuilder);
          });
    }
    if (!CollectionUtils.isEmpty(fjwtClaimContributors)) {
      fjwtClaimContributors.forEach(
          contributor -> {
            log.debug("retrieving claims from user using [{}]", contributor.getClass().getName());
            Map<String, Object> claims = contributor.getClaims(source);
            if (!CollectionUtils.isEmpty(claims)) {
              claims.forEach(claimsBuilder::add);
            }
          });
    }
    return claimsBuilder.build();
  }

  /**
   * Invokes the extractors chain adding information to the user
   *
   * @param source source map
   * @param dest destination user
   */
  public void addData(Claims source, FjwtAbstractUserDetailsBuilder dest) {

    log.debug(
        "found [{}] extractors in chain",
        Objects.nonNull(fjwtClaimsExtractors) ? fjwtClaimsExtractors.size() : 0);

    if (!CollectionUtils.isEmpty(fjwtClaimsExtractors)) {
      fjwtClaimsExtractors.forEach(
          ce -> {
            log.debug("retrieving claims from token using [{}]", ce.getClass().getName());
            ce.addData(source, dest);
          });
    }
  }
}
