package com.enginious.fjwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The authentication request */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtRequest {

  /** The username */
  private String username;

  /** The password */
  private String password;
}
