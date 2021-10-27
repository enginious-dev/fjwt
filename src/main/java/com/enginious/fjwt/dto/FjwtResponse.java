package com.enginious.fjwt.dto;

import lombok.*;

/**
 * The authentication response
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtResponse {

    /**
     * The jwt token
     */
    private String token;
}
