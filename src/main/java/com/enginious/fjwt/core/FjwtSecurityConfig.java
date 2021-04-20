package com.enginious.fjwt.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class FjwtSecurityConfig extends WebSecurityConfigurerAdapter {

    private final FjwtEntryPoint fjwtEntryPoint;
    private final UserDetailsService userDetailsService;
    private final FjwtRequestFilter fjwtRequestFilter;
    private FjwtNoAuthUrlHolder fjwtNoAuthUrlHolder;
    private final String url;

    public FjwtSecurityConfig(FjwtEntryPoint fjwtEntryPoint, UserDetailsService userDetailsService, FjwtRequestFilter fjwtRequestFilter, Optional<FjwtNoAuthUrlHolder> fjwtNoAuthUrlHolder, @Value("${fjwt.url:/authenticate}") String url) {
        super();
        this.fjwtEntryPoint = fjwtEntryPoint;
        this.userDetailsService = userDetailsService;
        this.fjwtRequestFilter = fjwtRequestFilter;
        fjwtNoAuthUrlHolder.ifPresent(b -> this.fjwtNoAuthUrlHolder = b);
        this.url = url;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .authorizeRequests().antMatchers(noAuthPaths()).permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(fjwtEntryPoint)
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(fjwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    protected String[] noAuthPaths() {
        return Objects.nonNull(fjwtNoAuthUrlHolder) ? Stream.concat(Arrays.stream(new String[]{url}), Arrays.stream(fjwtNoAuthUrlHolder.getNoAuthUrls())).toArray(String[]::new) : new String[]{url};
    }
}
