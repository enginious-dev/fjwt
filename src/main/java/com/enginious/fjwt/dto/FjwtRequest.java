package com.enginious.fjwt.dto;

import lombok.*;

/**
 * The authentication request
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtRequest {

    /**
     * The username
     */
    private String username;

    /**
     * The password
     */
    private String password;
}
