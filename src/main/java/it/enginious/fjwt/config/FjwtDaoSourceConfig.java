package it.enginious.fjwt.config;

import static it.enginious.fjwt.config.FjwtConfigCommon.DEFAULT_BEAN_REGISTRATION_PATTERN;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import it.enginious.fjwt.core.FjwtDummyUserDetailsService;

/**
 * Fjwt dao source configuration.
 *
 * @author Giuseppe Milazzo
 * @since 3.5.3
 */
@Slf4j
@Configuration
@Conditional(FjwtInteractiveAuthenticationCondition.class)
@ConditionalOnProperty(
    prefix = "fjwt",
    name = "userSource",
    havingValue = "DAO",
    matchIfMissing = true)
public class FjwtDaoSourceConfig {

  /**
   * register the default delegating {@link PasswordEncoder}. Existing BCrypt hashes without an
   * encoding prefix remain supported.
   *
   * @return the default password encoder bean
   */
  @Bean
  @ConditionalOnMissingBean(PasswordEncoder.class)
  @ConditionalOnProperty(
      prefix = "fjwt",
      name = "userSource",
      havingValue = "DAO",
      matchIfMissing = true)
  public PasswordEncoder passwordEncoder() {

    log.debug(
        DEFAULT_BEAN_REGISTRATION_PATTERN,
        DelegatingPasswordEncoder.class.getName(),
        PasswordEncoder.class.getName());
    DelegatingPasswordEncoder passwordEncoder =
        (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
    passwordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
    return passwordEncoder;
  }

  /**
   * register the default {@link UserDetailsService}
   *
   * @param passwordEncoder the password encoder
   * @return the default user details service bean
   */
  @Bean
  @ConditionalOnMissingBean(UserDetailsService.class)
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {

    log.warn(
        "registering bean of type [{}] as default [{}]: you should use this bean for testing "
            + "purposes only",
        FjwtDummyUserDetailsService.class.getName(),
        UserDetailsService.class.getName());
    return new FjwtDummyUserDetailsService(passwordEncoder);
  }

  /**
   * register a DAO-based {@link AuthenticationManager}
   *
   * @param passwordEncoder the password encoder
   * @param userDetailsService the user details service
   * @return the authentication manager bean
   */
  @Bean
  public AuthenticationManager authenticationManager(
      PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
    log.debug(
        "configuring [{}] with userDetailsService as [{}] and passwordEncoder as [{}]",
        AuthenticationManager.class.getName(),
        userDetailsService.getClass().getName(),
        passwordEncoder.getClass().getName());
    DaoAuthenticationProvider authenticationProvider =
        new DaoAuthenticationProvider(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authenticationProvider);
  }
}
