package it.enginious.fjwt.dto;

import lombok.*;

/**
 * The authentication response.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
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
