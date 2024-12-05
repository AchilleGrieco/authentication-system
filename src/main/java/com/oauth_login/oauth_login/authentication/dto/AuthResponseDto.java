package com.oauth_login.oauth_login.authentication.dto;

import lombok.Data;

@Data
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer ";
    private String username;
    private Long userId;
    private String error;

    public AuthResponseDto(String accessToken, String refreshToken, String username, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.userId = userId;
        this.error = null;
    }

    public AuthResponseDto(String accessToken, String refreshToken, String username, Long userId, String error) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = username;
        this.userId = userId;
        this.error = error;
    }
}
