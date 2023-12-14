package it.enginious.fjwt.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Fjwt auto configuration.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@ComponentScan(basePackages = "it.enginious.fjwt.core")
public class FjwtAutoconfiguration {

    /**
     * Logs if the configuration has been loaded into the context.
     */
    @PostConstruct
    protected void init() {

        log.debug("configuration loaded");
    }
}
