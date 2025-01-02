package com.authentication_system.authentication_system.authentication.oauth2;

import com.authentication_system.authentication_system.authentication.dto.AuthResponseDto;
import com.authentication_system.authentication_system.authentication.model.RefreshToken;
import com.authentication_system.authentication_system.authentication.model.UserEntity;
import com.authentication_system.authentication_system.authentication.security.JWTGenerator;
import com.authentication_system.authentication_system.authentication.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JWTGenerator jwtGenerator;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        
        UserEntity user = (UserEntity) authentication.getPrincipal();
        String token = jwtGenerator.generateToken(authentication);
        
        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());

        // Create authentication response
        AuthResponseDto authResponse = new AuthResponseDto(
            token,
            refreshToken.getToken(),
            "Bearer",
            user.getUsername(),
            user.getUserId()
        );

        // Set response content type
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Write the authentication response as JSON
        objectMapper.writeValue(response.getOutputStream(), authResponse);
    }
}
