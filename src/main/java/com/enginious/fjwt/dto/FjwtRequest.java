package com.enginious.fjwt.dto;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FjwtRequest {

    private String username;
    private String password;
}
