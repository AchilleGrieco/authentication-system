package com.authentication_system.authentication_system.authentication.dto;

import lombok.Data;

@Data
public class AuthResponseDto {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String username;
    private Long userId;
    private String error;

    public AuthResponseDto(String accessToken, String refreshToken, String tokenType, String username, Long userId) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.username = username;
        this.userId = userId;
        this.error = null;
    }

    public AuthResponseDto(String accessToken, String refreshToken, String tokenType, String username, Long userId, String error) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.username = username;
        this.userId = userId;
        this.error = error;
    }

    public AuthResponseDto(String error) {
        this.error = error;
    }
}
