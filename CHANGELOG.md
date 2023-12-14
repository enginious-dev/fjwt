# Changelog
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