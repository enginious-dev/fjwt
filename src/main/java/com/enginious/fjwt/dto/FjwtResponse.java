package com.enginious.fjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The authentication response.
 *
 * @since 1.0.0
 * @author Giuseppe Milazzo
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtResponse {

  /** The jwt token */
  private String token;
}
