package com.enginious.fjwt.core;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Fjwt web security configuration.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FjwtWebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final FjwtEntryPoint fjwtEntryPoint;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService;
  private final FjwtRequestFilter fjwtRequestFilter;
  private final FjwtConfig fjwtConfig;

  /** {@inheritDoc} */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

    log.debug(
        "configuring [{}] with userDetailsService as [{}] and passwordEncoder as [{}]",
        AuthenticationManagerBuilder.class.getName(),
        userDetailsService.getClass().getName(),
        passwordEncoder.getClass().getName());
    auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
  }

  /** {@inheritDoc} */
  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {

    log.debug("registering [{}] using parent method", AuthenticationManager.class.getName());
    return super.authenticationManagerBean();
  }

  /** {@inheritDoc} */
  @Override
  protected void configure(HttpSecurity httpSecurity) throws Exception {

    log.debug(
        "configuring [{}]: paths that don't need authentication are [{}]",
        HttpSecurity.class.getName(),
        Stream.concat(
                Arrays.stream(new String[] {fjwtConfig.getEndpoint()}),
                fjwtConfig.getUnsecured().stream())
            .collect(Collectors.joining(", ")));

    httpSecurity
        .csrf()
        .disable()
        .authorizeRequests()
        .antMatchers(
            Stream.concat(
                    Arrays.stream(new String[] {fjwtConfig.getEndpoint()}),
                    fjwtConfig.getUnsecured().stream())
                .toArray(String[]::new))
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(fjwtEntryPoint)
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    httpSecurity.addFilterBefore(fjwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
  }
}
