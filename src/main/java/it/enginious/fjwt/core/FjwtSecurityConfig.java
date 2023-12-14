package it.enginious.fjwt.core;

import it.enginious.fjwt.core.extractors.FjwtAuthoritiesExtractor;
import it.enginious.fjwt.core.extractors.FjwtUserDetailsFlagsExtractor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provides {@link PasswordEncoder} and/or {@link UserDetailsService} beans they are missing. The
 * default for {@link PasswordEncoder} is {@link BCryptPasswordEncoder}, while for the {@link
 * UserDetailsService} is a service that always returns a user with username and password equal to
 * the username passed.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class FjwtSecurityConfig {

    private static final String DEFAULT_BEAN_REGISTRATION_PATTERN =
            "registering bean of type [{}] as default [{}]";
    private static final String DEFAULT_EXTRACTORS_BEAN_REGISTRATION_PATTERN =
            "registering bean of type [{}] as default [{}], if you want to exclude the default extractors set the property fjwt.enableDefaultExtractors to false";

    /**
     * register the default {@link PasswordEncoder}
     *
     * @return the default password encoder bean
     */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {

        log.debug(
                DEFAULT_BEAN_REGISTRATION_PATTERN,
                BCryptPasswordEncoder.class.getName(),
                PasswordEncoder.class.getName());
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

        log.warn(
                "registering bean of type [{}] as default [{}]: you should use this bean for testing purposes only",
                FjwtDummyUserDetailsService.class.getName(),
                UserDetailsService.class.getName());
        return new FjwtDummyUserDetailsService(passwordEncoder);
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

        log.debug(
                DEFAULT_BEAN_REGISTRATION_PATTERN,
                FjwtClaimsExtractorChain.class.getName(),
                FjwtClaimsExtractorChain.class.getName());
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

        log.debug(
                DEFAULT_BEAN_REGISTRATION_PATTERN,
                FjwtSimpleUserDetailsBuilder.class.getName(),
                FjwtUserDetailsBuilderFactory.class.getName());
        return FjwtSimpleUserDetailsBuilder::new;
    }

    /**
     * register a {@link FjwtAuthoritiesExtractor} bean
     *
     * @return a {@link FjwtAuthoritiesExtractor} bean
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "fjwt",
            name = "enableDefaultExtractors",
            havingValue = "true",
            matchIfMissing = true)
    public FjwtAuthoritiesExtractor authoritiesExtractor() {

        log.debug(
                DEFAULT_EXTRACTORS_BEAN_REGISTRATION_PATTERN,
                FjwtAuthoritiesExtractor.class.getName(),
                FjwtClaimsExtractor.class.getName());
        return new FjwtAuthoritiesExtractor();
    }

    /**
     * register a {@link FjwtUserDetailsFlagsExtractor} bean
     *
     * @return a {@link FjwtUserDetailsFlagsExtractor} bean
     */
    @Bean
    @ConditionalOnProperty(
            prefix = "fjwt",
            name = "enableDefaultExtractors",
            havingValue = "true",
            matchIfMissing = true)
    public FjwtUserDetailsFlagsExtractor userDetailsFlagsExtractor() {

        log.debug(
                DEFAULT_EXTRACTORS_BEAN_REGISTRATION_PATTERN,
                FjwtUserDetailsFlagsExtractor.class.getName(),
                FjwtClaimsExtractor.class.getName());
        return new FjwtUserDetailsFlagsExtractor();
    }

    /**
     * register the default {@link FjwtTokenInvalidator}
     *
     * @return the default token invalidator bean
     */
    @Bean
    @ConditionalOnMissingBean(FjwtTokenInvalidator.class)
    public FjwtTokenInvalidator tokenInvalidator() {

        log.warn(
                DEFAULT_BEAN_REGISTRATION_PATTERN,
                NoopTokenInvalidator.class.getName(),
                FjwtTokenInvalidator.class.getName());
        return new NoopTokenInvalidator();
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

        log.debug("registering bean of type [{}] as default", Clock.class.getName());
        return Clock.system(StringUtils.isBlank(zoneId) ? ZoneId.systemDefault() : ZoneId.of(zoneId));
    }
}
