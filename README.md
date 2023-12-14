# Fjwt

### A fast and simple JWT authentication library

## What this library does?

This library allows you to enable jwt authentication in any spring boot project with a few simple
steps. Once the dependency is added to the classpath the autoconfiguration enables this
authentication method. The authentication configuration can be done through
spring's `application.yml`, for example:

```
fjwt:
  endpoint: /your-auth-path      # Jwt authentication endpoint, default is "/authenticate"
  unsecured:                     # List of paths that do not need authentication (the above one is already included)
  - /some-other-path/**
  ttl: 600                       # Jwt token ttl in seconds, default is 3600
  secret: your-strong-key        # Server secret
  zoneId: ECT                    # Server timezone from java.time.ZoneId#SHORT_IDS, if blank java.time.ZoneId#systemDefault() will be used
  algorithm: HS256               # Jwt token signature algorithm, default is HS512
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
bean of type `PasswordEncoder`, in any case the library by default provides you the
`BCryptPasswordEncoder`.

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