package com.enginious.fjwt.config;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add this annotation to an {@link org.springframework.context.annotation.Configuration} or
 * {@link org.springframework.boot.autoconfigure.SpringBootApplication} class to have
 * the Fjwt configured, for example:
 * <pre class="code">
 * &#064;EnableFjwt
 * &#064;Configuration
 * public class MySecurityConfiguration{
 * ...
 * }
 * </pre>
 * or
 * <pre class="code">
 * &#064;EnableFjwt
 * &#064;SpringBootApplication
 * public class MySpringBootApplication{
 * ...
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(FjwtConfig.class)
public @interface EnableFjwt {
}
