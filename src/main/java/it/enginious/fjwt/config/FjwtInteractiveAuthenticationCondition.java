package it.enginious.fjwt.config;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Matches modes that authenticate a resource owner locally.
 *
 * @author Giuseppe Milazzo
 * @since 4.2.0
 */
class FjwtInteractiveAuthenticationCondition extends AnyNestedCondition {

  FjwtInteractiveAuthenticationCondition() {
    super(ConfigurationPhase.REGISTER_BEAN);
  }

  @ConditionalOnProperty(
      prefix = "fjwt",
      name = "security-mode",
      havingValue = "LEGACY_JWT",
      matchIfMissing = true)
  static class LegacyJwt {}

  @ConditionalOnProperty(
      prefix = "fjwt",
      name = "security-mode",
      havingValue = "OAUTH2_AUTHORIZATION_SERVER")
  static class OAuth2AuthorizationServer {}
}
