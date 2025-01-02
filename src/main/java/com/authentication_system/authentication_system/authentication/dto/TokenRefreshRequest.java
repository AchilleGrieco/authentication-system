package com.authentication_system.authentication_system.authentication.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refreshToken;
}
