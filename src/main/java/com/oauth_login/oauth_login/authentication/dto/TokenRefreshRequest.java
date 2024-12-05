package com.oauth_login.oauth_login.authentication.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
