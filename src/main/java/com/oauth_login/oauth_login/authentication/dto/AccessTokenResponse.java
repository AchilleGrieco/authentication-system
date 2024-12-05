package com.oauth_login.oauth_login.authentication.dto;

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
