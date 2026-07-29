# Fjwt

### A fast and simple JWT authentication library

## What this library does?

This library allows you to enable jwt authentication in any spring boot project with a few simple
steps. Once the dependency is added to the classpath the autoconfiguration enables this
authentication method. The authentication configuration can be done through
spring's `application.yml`, for example:

```
fjwt:
  security-mode: LEGACY_JWT    # LEGACY_JWT (default), OAUTH2_RESOURCE_SERVER or OAUTH2_AUTHORIZATION_SERVER
  endpoint: /your-auth-path      # Jwt authentication endpoint, default is "/authenticate"
  unsecured:                     # List of paths that do not need authentication (the above one is already included)
  - /some-other-path/**
  ttl: 600                       # Jwt token ttl in seconds, default is 3600
  secret: your-strong-key        # Server secret
  zoneId: ECT                    # Server timezone from java.time.ZoneId#SHORT_IDS, if blank java.time.ZoneId#systemDefault() will be used
  algorithm: HS256               # Jwt token signature algorithm, default is HS256
  enableDefaultExtractors: true  # Default FjwtClaimsExtractor enabling flag, don't worry, it will be cleared up later
```

if you do not provide the value for the property `fjwt.secret` a random key will be generated at
runtime according to the chosen algorithm that. You will find the generated key in the logs by setting log level
for `core.it.enginious.fjwt.FjwtTokenUtil` to `TRACE`, as below:

```
15:27:02.629 [main] [INFO ] core.it.enginious.fjwt.FjwtTokenUtil: no secret provided: "some-random-key" (generated with algorithm {}) will be used
```

Don't forget to define a bean of type `UserDetailsService`... If you don't do, the default
implementation, `FjwtDummyUserDetailsService` will be used which is a
`UserDetailsService` that for any username passed returns a user with username and password equal to
the one requested but, as above, it is not the best of the safety. Optionally you can also define
bean of type `PasswordEncoder`. The default is a delegating password encoder that creates
`{bcrypt}` hashes and can also verify legacy BCrypt hashes without an encoding prefix.

The properties `endpoint`, `ttl`, `secret`, `zoneId`, and `algorithm` apply to `LEGACY_JWT` mode.

## OAuth 2.0 Resource Server

Use this mode when the application exposes APIs and accepts access tokens issued by an external
authorization server such as Keycloak, Microsoft Entra ID, Auth0, or another Fjwt application
running in authorization-server mode.

OAuth dependencies are marked as `provided` by Fjwt, so add the Resource Server starter to the
application:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
</dependency>
```

Configure issuer and audience through the standard Spring Boot properties:

```yaml
fjwt:
  security-mode: OAUTH2_RESOURCE_SERVER
  unsecured:
    - /actuator/health
  oauth2:
    authorities-claim: authorities # application roles/permissions claim
    authority-prefix: ""            # optional prefix for application authorities
    principal-claim: sub            # claim used as Authentication#getName()

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://identity.example.com
          audiences:
            - https://api.example.com
```

The standard `scope`/`scp` claim is mapped to `SCOPE_*` authorities. Values in
`fjwt.oauth2.authorities-claim` are added separately, preserving application roles such as
`ROLE_ADMIN`. Signature, expiration, issuer, and audience validation is performed by Spring
Security using the authorization server metadata and JWK Set.

The legacy `/authenticate` endpoint, JJWT parser, and `FjwtRequestFilter` are not registered in this
mode.

## OAuth 2.0 Authorization Server and OpenID Connect

Use this mode when the application itself must register OAuth clients and issue authorization
codes, access tokens, and refresh tokens.

Add the Authorization Server starter to the application:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security-oauth2-authorization-server</artifactId>
</dependency>
```

A minimal client using Authorization Code, PKCE, and refresh tokens can be configured as follows:

```yaml
fjwt:
  security-mode: OAUTH2_AUTHORIZATION_SERVER
  oauth2:
    oidc-enabled: true

spring:
  security:
    oauth2:
      authorizationserver:
        issuer: https://identity.example.com
        client:
          web-client:
            registration:
              client-id: web-client
              client-secret: "{bcrypt}<encoded-secret>"
              client-authentication-methods:
                - client_secret_basic
              authorization-grant-types:
                - authorization_code
                - refresh_token
              redirect-uris:
                - https://client.example.com/login/oauth2/code/web-client
              post-logout-redirect-uris:
                - https://client.example.com/
              scopes:
                - openid
                - profile
                - api.read
            require-authorization-consent: true
```

Client secrets must be encoded using the configured `PasswordEncoder`; `{noop}` should only be
used in local tests. Public clients should use Authorization Code with PKCE and no client secret.
The Resource Owner Password Credentials grant is deliberately not implemented.

The server exposes the standard Spring Security endpoints, including:

- `/oauth2/authorize`
- `/oauth2/token`
- `/oauth2/revoke`
- `/oauth2/introspect`
- `/oauth2/jwks`
- `/.well-known/oauth-authorization-server`
- `/.well-known/openid-configuration` when OIDC is enabled

DAO and LDAP user sources continue to authenticate resource owners. Registered OAuth clients are
configured through `spring.security.oauth2.authorizationserver.client.*` or by defining a custom
`RegisteredClientRepository` bean.

For production, provide persistent implementations of `RegisteredClientRepository`,
`OAuth2AuthorizationService`, and `OAuth2AuthorizationConsentService`, and provide a persistent
`JWKSource<SecurityContext>` backed by a managed RSA or EC key. Spring Boot's default in-memory
repositories and generated RSA key are intended for development and make tokens and grants invalid
after a restart. Always configure an HTTPS issuer in production.

### OAuth 2.0 custom claims

Existing `FjwtClaimsExtractor` beans are reused when access tokens are issued for an authenticated
user. New integrations can avoid a dependency on JJWT by registering one or more
`FjwtClaimContributor` beans:

```java
@Bean
FjwtClaimContributor tenantClaimContributor() {
  return user -> Map.of("tenant", ((MyUser) user).getTenant());
}
```

Custom contributors are used by both legacy JWT generation and the OAuth 2.0 Authorization Server.
OAuth reserved claims such as `iss`, `sub`, `aud`, `exp`, `jti`, `client_id`, and `scope` cannot be
overridden by a contributor.

## Enrich or modify the information present in the token

To enrich or modify the information present in the token you can modify the
property `fjwt.enableDefaultExtractors` (which by default is `true`) and/or register beans that
extend the `FjwtClaimsExtractor` interface. This interface defines two methods:

```
void getClaims(UserDetails source, Claims dest)
```

that extracts information from the user and adds it to the token, and

``` 
void addData(Claims source, FjwtAbstractUserDetailsBuilder dest)
```

that extracts the previous added information from the token and adds it to the user.

The two extractors that the library offers you are `FjwtAuthoritiesExtractor` and
`FjwtUserDetailsFlagsExtractor`, the first adds all the user authorities to the token while the
second adds all the flags. Do not forget that if you use an enriched implementation of `UserDetails`
you should also define a bean of type `FjwtAbstractUserDetailsBuilder` to make sure that your custom
fields can be recovered. By default, library defines a `FjwtSimpleUserDetailsBuilder` which, unless
additional fields, is enough for all situations.

## Token Invalidator

This library has built-in support for token invalidation. The interface `FjwtTokenInvalidator` offers
two methods to implement your custom token invalidation strategy:

```
void store(UserDetails source, String token) throws FjwtTokenInvalidatorException;
```

that should store token anywhere you want, and

``` 
boolean wasInvalidated(UserDetails source, String token) throws FjwtTokenInvalidatorException;
``` 

that should check if a token was invalidated.

By default, a bean of type `NoopTokenInvalidator` is registered in the application context that not
performs any storage operation or invalidation check. To replace this behaviour you simply have to
implement your strategy by implementing the previous interface and register an instance as bean.
For example:

``` 
@Component
public class RedisTokenInvalidator implements FjwtTokenInvalidator{
  @Override
  public void store(UserDetails source, String token) throws FjwtTokenInvalidatorException {
    // do your stuff here
  }
  @Override
  public boolean wasInvalidated(UserDetails source, String token) throws FjwtTokenInvalidatorException {
    // do your stuff here
  }
}
```

In this simple way, the registered bean will replace the default `NoopTokenInvalidator` and your logic
will run.

> **NB:** By default, when a `FjwtTokenInvalidatorException` is thrown the chain will break, so this means
> that user will receive `401 - UNAUTHORIZED` both in case of login attempt at the endpoint specified through
> `fjwt.endpoint` property and for any other request.

## How to test?

If you want to test add this the dependency to you `pom.xml`:

```
<dependency>
    <groupId>it.enginious</groupId>
    <artifactId>fjwt</artifactId>
    <version> <!-- check last version available on https://central.sonatype.dev/namespace/it.enginious --> </version>
</dependency>
```

configure it as shown above and start the application, then do `POST` to the path of your
configured (maybe the default one could be a good choice) with the following body:

```
{
    "username": "some-strange-username",
    "password": "some-strange-password"
}
```

to receive a response like:

```
{
    "token": "some-generated-jwt-token"
}
```

from that moment and until the token is valid you will be able to access the various resources of
your application that require authentication by adding the `Authorization` key in the request header
with the `Bearer ` value concatenated within the obtained token (for
example `Bearer some-generated-jwt-token`).
