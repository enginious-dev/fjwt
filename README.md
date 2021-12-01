# Fjwt

### A fast and simple JWT authentication library

## What this library does?

This library allows you to enable jwt authentication in any spring boot project with a few simple
steps. Once the dependency is added to the classpath the autoconfiguration enables this
authentication method. The authentication configuration can be done through
spring's `application.yml`, for example:

```
fjwt:
  endpoint: /your-auth-path  # Jwt authentication endpoint, default is "/authenticate"
  unsecured:                 # List of paths that do not need authentication (the above one is already included)
  - /some-other-path/**
  ttl: 600                   # Jwt token ttl in seconds, default is 3600
  secret: your-strong-key    # Server secret, default is "secret"
  zoneId: ECT                # Server timezone from java.time.ZoneId#SHORT_IDS, if blank java.time.ZoneId#systemDefault() will be used
  algorithm: HS256           # Jwt token signature algorithm, default is HS512
```

if you do not provide the value for the property `fjwt.secret` a random key will be generated at
runtime according to the chosen algorithm that. You will find the generated key in the logs, as
below:

```
15:27:02.629 [main] [INFO ] c.enginious.fjwt.core.FjwtTokenUtil: no secret provided, generating one
15:27:02.634 [main] [INFO ] c.enginious.fjwt.core.FjwtTokenUtil: generated secret is: "some-random-key"
```

Don't forget to define a bean of type `UserDetailsService`... If you don't do, the default
implementation, `FjwtDummyUserDetailsService` will be used which is a
`UserDetailsService` that for any username passed returns a user with username and password equal to
the one requested but, as above, it is not the best of the safety. Optionally you can also define
bean of type `PasswordEncoder`, in any case the library by default provides you the
`BCryptPasswordEncoder`.

## How to test?

If you want to test add this the dependency to you `pom.xml`:

```
<dependency>
    <groupId>com.enginious-dev</groupId>
    <artifactId>fjwt</artifactId>
    <version>1.0.0</version>
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