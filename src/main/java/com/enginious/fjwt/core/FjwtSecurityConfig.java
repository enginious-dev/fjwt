package com.enginious.fjwt.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

/**
 * Provides {@link PasswordEncoder} and/or {@link UserDetailsService} beans they are missing.
 * The default for {@link PasswordEncoder} is {@link BCryptPasswordEncoder}, while for the
 * {@link UserDetailsService} is a service that always returns a user with username and password
 * equal to the username passed.
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
}
