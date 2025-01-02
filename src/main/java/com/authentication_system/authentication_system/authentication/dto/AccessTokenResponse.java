package com.authentication_system.authentication_system.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessTokenResponse {
    private String accessToken;
    private String tokenType = "Bearer";

    public AccessTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
