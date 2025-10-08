package it.enginious.fjwt.config;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.ldap.LdapConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

/**
 * Fjwt ldap source configuration.
 *
 * @author Giuseppe Milazzo
 * @since 3.5.3
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "fjwt", name = "userSource", havingValue = "LDAP")
public class FjwtLdapSourceConfig {

  private final FjwtProperties fjwtProperties;

  /**
   * register the default {@link LdapConnectionDetails} based on {@link FjwtProperties#ldap}
   *
   * @return the LdapConnectionDetails
   */
  @Bean
  public LdapConnectionDetails ldapConnectionDetails() {
    return new LdapConnectionDetails() {
      @Override
      public String[] getUrls() {
        return fjwtProperties.getLdap().getUrls();
      }

      @Override
      public String getBase() {
        return fjwtProperties.getLdap().getBase();
      }

      @Override
      public String getUsername() {
        return fjwtProperties.getLdap().getUsername();
      }

      @Override
      public String getPassword() {
        return fjwtProperties.getLdap().getPassword();
      }
    };
  }

  @Bean
  public LdapContextSource ldapContextSource(LdapConnectionDetails ldapConnectionDetails) {
    var ldapContextSource =
        new DefaultSpringSecurityContextSource(
            List.of(ldapConnectionDetails.getUrls()), ldapConnectionDetails.getBase());
    ldapContextSource.setUserDn(ldapConnectionDetails.getUsername());
    ldapContextSource.setPassword(ldapConnectionDetails.getPassword());
    return ldapContextSource;
  }

  @Bean
  public BindAuthenticator bindAuthenticator(LdapContextSource ldapContextSource) {
    var authenticator = new BindAuthenticator(ldapContextSource);
    authenticator.setUserSearch(
        new FilterBasedLdapUserSearch(
            fjwtProperties.getLdap().getUserSearchBase(),
            fjwtProperties.getLdap().getUserSearchFilter(),
            ldapContextSource));
    return authenticator;
  }

  @Bean
  public LdapAuthoritiesPopulator ldapAuthoritiesPopulator(LdapContextSource ldapContextSource) {
    var ldapAuthoritiesPopulator =
        new DefaultLdapAuthoritiesPopulator(
            ldapContextSource, fjwtProperties.getLdap().getGroupSearchBase());
    ldapAuthoritiesPopulator.setGroupSearchFilter(fjwtProperties.getLdap().getGroupSearchFilter());
    return ldapAuthoritiesPopulator;
  }

  /**
   * register an LDAP-based {@link LdapConnectionDetails}
   *
   * @param ldapConnectionDetails the LdapConnectionDetails
   * @return the authentication manager bean
   */
  @Bean
  public AuthenticationManager authenticationManager(
      BindAuthenticator bindAuthenticator, LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
    var provider = new LdapAuthenticationProvider(bindAuthenticator, ldapAuthoritiesPopulator);
    return new ProviderManager(provider);
  }
}
