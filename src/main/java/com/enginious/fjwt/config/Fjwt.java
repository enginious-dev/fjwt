package com.enginious.fjwt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Fjwt configuration, use {@link EnableFjwt} instead of import this class
 * with {@link org.springframework.context.annotation.Import}.
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.enginious.fjwt.core")
public class Fjwt {

    @PostConstruct
    protected void init() {
        log.info("configuration loaded");
    }
}
