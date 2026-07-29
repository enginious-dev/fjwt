package it.enginious.fjwt.core;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Contributes custom claims without depending on a specific JWT implementation.
 *
 * @author Giuseppe Milazzo
 * @since 4.2.0
 */
@FunctionalInterface
public interface FjwtClaimContributor {

  /**
   * Extract claims from a user.
   *
   * @param source source user
   * @return claims to add to the token
   */
  Map<String, Object> getClaims(UserDetails source);
}
