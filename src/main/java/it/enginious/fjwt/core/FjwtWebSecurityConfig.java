package it.enginious.fjwt.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Fjwt web security configuration.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class FjwtWebSecurityConfig {

    private final FjwtEntryPoint fjwtEntryPoint;
    private final FjwtRequestFilter fjwtRequestFilter;
    private final FjwtConfig fjwtConfig;

    /**
     * register the {@link AuthenticationManager}
     *
     * @param passwordEncoder    the password encoder
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
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    /**
     * register the {@link SecurityFilterChain}
     *
     * @param httpSecurity the http security
     * @return the security filter chain bean
     * @throws Exception if any error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        log.debug(
                "configuring [{}]: paths that don't need authentication are [{}]",
                HttpSecurity.class.getName(),
                String.join(", ", fjwtConfig.getAllUnsecuredEndpoints()));

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> auth.requestMatchers(fjwtConfig.getAllUnsecuredEndpoints()).permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(
                        exceptionHandlingConfigurer ->
                                exceptionHandlingConfigurer.authenticationEntryPoint(fjwtEntryPoint))
                .sessionManagement(
                        sessionManagementConfigurer ->
                                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity
                .addFilterBefore(fjwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
