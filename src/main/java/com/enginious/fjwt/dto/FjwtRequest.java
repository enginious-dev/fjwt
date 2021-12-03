package com.enginious.fjwt.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The authentication request.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtRequest {

  /** The username */
  @NotBlank(message = "username is mandatory")
  private String username;

  /** The password */
  private String password;
}
