package com.enginious.fjwt.core;

import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fjwt")
public class FjwtConfig {

    private String endpoint = "/authenticate";
    private List<String> unsecured = new ArrayList<>();
    private int ttl = 3600;
    private String secret = "secret";
    private SignatureAlgorithm algorithm = SignatureAlgorithm.HS512;
}
