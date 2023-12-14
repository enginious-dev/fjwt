package it.enginious.fjwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * The authentication request.
 *
 * @author Giuseppe Milazzo
 * @since 1.0.0
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
    @NotBlank(message = "username is mandatory")
    private String username;

    /**
     * The password
     */
    private String password;
}
