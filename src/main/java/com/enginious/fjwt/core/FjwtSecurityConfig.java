package com.enginious.fjwt.core;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides {@link PasswordEncoder} and/or {@link UserDetailsService} beans they are missing. The
 * default for {@link PasswordEncoder} is {@link BCryptPasswordEncoder}, while for the {@link
 * UserDetailsService} is a service that always returns a user with username and password equal to
 * the username passed.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Configuration
public class FjwtSecurityConfig {

  /**
   * register the default {@link PasswordEncoder}
   *
   * @return the default password encoder bean
   */
  @Bean
  @ConditionalOnMissingBean(PasswordEncoder.class)
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
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
    return s -> new User(s, passwordEncoder.encode(s), Collections.emptyList());
  }

  /**
   * register the default {@link FjwtClaimsExtractorChain}
   *
   * @param extractors all registered beans of type {@link FjwtClaimsExtractor}
   * @return the default claim extractor bean
   */
  @Bean
  @ConditionalOnMissingBean(FjwtClaimsExtractorChain.class)
  public FjwtClaimsExtractorChain claimsExtractorChain(
      Optional<List<FjwtClaimsExtractor>> extractors) {
    return new FjwtClaimsExtractorChain(extractors.orElse(new ArrayList<>()));
  }

  /**
   * register the default {@link FjwtUserDetailsBuilderFactory}
   *
   * @return the default user details builder factory bean
   */
  @Bean
  @ConditionalOnMissingBean(FjwtUserDetailsBuilderFactory.class)
  public FjwtUserDetailsBuilderFactory userDetailsBuilderFactory() {
    return FjwtSimpleUserDetailsBuilder::new;
  }

  /**
   * register the default {@link Clock}
   *
   * @param zoneId the zone id
   * @return the default clock bean
   */
  @Bean
  @ConditionalOnMissingBean(Clock.class)
  public Clock clock(@Value("${fjwt.zoneId:}") String zoneId) {
    return Clock.system(StringUtils.isBlank(zoneId) ? ZoneId.systemDefault() : ZoneId.of(zoneId));
  }
}
