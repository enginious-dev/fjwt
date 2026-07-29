# Changelog

## Unreleased
#### Enhancements:
- added backward-compatible security modes for legacy JWT, OAuth 2.0 Resource Server, and OAuth 2.0 Authorization Server
- added optional OpenID Connect support and standard OAuth 2.0 protocol endpoints
- added standard scope and application-authority mapping for OAuth 2.0 JWT access tokens
- added provider-independent `FjwtClaimContributor` support
- changed the default password encoder to a delegating encoder with fallback support for legacy BCrypt hashes

---

## v3.4.5 (?)
#### Enhancements:
- upgraded to java release 21
- upgraded dependency of io.github.hakky5/logcaptor to [2.9.3](https://github.com/Hakky54/log-captor/blob/master/CHANGELOG.MD)
- upgraded dependency of io.jsonwebtoken/jjwt-* to [0.12.6](https://github.com/jwtk/jjwt/blob/master/CHANGELOG.md)
- added com.diffplug.spotless:spotless-maven-plugin [2.43.0](https://github.com/diffplug/spotless/blob/main/plugin-maven/CHANGES.md#2443---2025-02-20)
- [[#3](https://github.com/enginious-dev/fjwt/issues/3)] added properties to customize frame option
- [[#1](https://github.com/enginious-dev/fjwt/issues/1)] upgraded Spring Boot dependencies to 3.3.5
- [[#5](https://github.com/enginious-dev/fjwt/issues/5)] upgraded Spring Boot dependencies to 3.4.5
#### Bug Fixes:
- [[#7](https://github.com/enginious-dev/fjwt/issues/7)] fix properties handling to customize frame option & chain bypass for OPTIONS http method calls

---
## v3.2.0 (14/12/2023)
#### Enhancements:
- changed versioning number policy in order to keep it in sync with Spring Boot dependencies used
- upgraded dependency of io.jsonwebtoken/jjwt-* to [0.12.3](https://github.com/jwtk/jjwt/blob/master/CHANGELOG.md)
#### Bug Fixes:
*No bug fixes for this release.*

---
## Old version history (before 14/12/2023)
- [61*] Move FjwtTokenInvalidatorException to more significant package
- [60*] Fix javadoc warnings
- [57*] Fix secret key generation
- [55*] Switch to shared shared-pipeline
- [50*] Bypass header check for unsecured endpoint(s)
- [49*] LCM - Spring boot 3.0.0
- [61*] Move FjwtTokenInvalidatorException to more significant package
- [60*] Fix javadoc warnings
- [57*] Fix secret key generation
- [55*] Switch to shared shared-pipeline
- [50*] Bypass header check for unsecured endpoint(s)
- [49*] LCM - Spring boot 3.0.0
- [37*] LCM - Spring boot 2.7.5
- [36*] QA - remove WebSecurityConfigurerAdapter in FjwtWebSecurityConfig
- [33*] LCM - Spring boot 2.7.4
- [27*] Invalidate tokens
- [22*] Upgraded dependency management to spring-boot-dependencies 2.6.3
- [19*] Added login request validation
- [15*] Random secret generation support
- [14*] Standardized logs to make everything more understandable
- [12*] Upgraded dependency management to spring-boot-dependencies 2.6.0
- [10*] Added authorities and user flags to token
- [8*]( username check in FjwtRequestFilter
#### Bug Fixes:
- [46*] Enable @Secured annotation processing

---
(*) no issue link available since repository has been moved and old repo was deleted
