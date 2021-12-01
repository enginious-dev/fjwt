package com.enginious.fjwt.config;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Fjwt auto configuration.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Slf4j
@Configuration
@ComponentScan(basePackages = "com.enginious.fjwt.core")
public class FjwtAutoconfiguration {

  /** Logs if the configuration has been loaded into the context. */
  @PostConstruct
  protected void init() {
    log.info("configuration loaded");
  }
}
