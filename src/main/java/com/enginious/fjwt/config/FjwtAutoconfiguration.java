package com.enginious.fjwt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Fjwt auto configuration
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.enginious.fjwt.core")
public class FjwtAutoconfiguration {

    @PostConstruct
    protected void init() {
        log.info("configuration loaded");
    }
}
