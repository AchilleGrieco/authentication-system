package com.oauth_login.oauth_login.authentication.controller;

import com.oauth_login.oauth_login.authentication.dto.AccessTokenResponse;
import com.oauth_login.oauth_login.authentication.dto.AuthResponseDto;
import com.oauth_login.oauth_login.authentication.dto.LoginDto;
import com.oauth_login.oauth_login.authentication.dto.RegisterDto;
import com.oauth_login.oauth_login.authentication.dto.TokenRefreshRequest;
import com.oauth_login.oauth_login.authentication.model.AuthProvider;
import com.oauth_login.oauth_login.authentication.model.UserEntity;
import com.oauth_login.oauth_login.authentication.model.RefreshToken;
import com.oauth_login.oauth_login.authentication.repository.UserRepository;
import com.oauth_login.oauth_login.authentication.security.CustomUserDetails;
import com.oauth_login.oauth_login.authentication.security.JWTGenerator;
import com.oauth_login.oauth_login.authentication.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JWTGenerator jwtGenerator,
                         RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {

        if (registerDto.getUsername() == null) {
            return new ResponseEntity<>("Username can't be null", HttpStatus.BAD_REQUEST);
        }

        if (registerDto.getEmail() == null) {
            return new ResponseEntity<>("Email can't be null", HttpStatus.BAD_REQUEST);
        }

        if (registerDto.getPassword() == null) {
            return new ResponseEntity<>("Password can't be null", HttpStatus.BAD_REQUEST);
        }

        // Check if email exists and get the user to check the provider
        Optional<UserEntity> existingUser = userRepository.findByEmail(registerDto.getEmail());
        if (existingUser.isPresent()) {
            UserEntity user = existingUser.get();
            if (user.getProvider() != AuthProvider.LOCAL) {
                return new ResponseEntity<>(
                    "This email is already registered with " + user.getProvider() + 
                    ". Please use the appropriate social login.", 
                    HttpStatus.BAD_REQUEST
                );
            }
            return new ResponseEntity<>("Email is already registered!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setProvider(AuthProvider.LOCAL);

        userRepository.saveAndFlush(user);
        return new ResponseEntity<>("User Registered with success!", HttpStatus.OK);
    }

    @GetMapping("/oauth2/success")
    public ResponseEntity<AuthResponseDto> oauth2Success(@AuthenticationPrincipal OAuth2User oauth2User, 
                                                       OAuth2AuthenticationToken authentication) {
        String email = oauth2User.getAttribute("email");
        String providerId = oauth2User.getAttribute("sub");
        
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);
        
        UserEntity user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getProvider() == AuthProvider.LOCAL) {
                return new ResponseEntity<>(
                    new AuthResponseDto(null, null, null, null, 
                    "This email is already registered directly. Please login with your password."),
                    HttpStatus.BAD_REQUEST
                );
            }
            // Update existing OAuth2 user details if needed
            updateExistingOAuth2User(user, oauth2User);
        } else {
            // Create new OAuth2 user
            user = new UserEntity();
            user.setEmail(email);
            user.setUsername(oauth2User.getAttribute("name"));
            user.setProvider(determineProvider(authentication));
            user.setProviderId(providerId);
            user.setPassword(""); // OAuth2 users don't need a password
                        
            user = userRepository.save(user);
        }
        
        // Generate JWT token
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user.getUsername(), null, oauth2User.getAuthorities());
        String token = jwtGenerator.generateToken(auth);
        
        // Generate refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUserId());
        
        return new ResponseEntity<>(new AuthResponseDto(
            token, 
            refreshToken.getToken(),
            user.getUsername(), 
            user.getUserId(),
            null
        ), HttpStatus.OK);
    }

    private AuthProvider determineProvider(OAuth2AuthenticationToken authentication) {
        String registrationId = authentication.getAuthorizedClientRegistrationId().toUpperCase();
        return AuthProvider.valueOf(registrationId);
    }

    private void updateExistingOAuth2User(UserEntity user, OAuth2User oauth2User) {
        user.setUsername(oauth2User.getAttribute("name"));
        String email = oauth2User.getAttribute("email");
        if (email != null && !email.equals(user.getEmail())) {
            user.setEmail(email);
        }
        userRepository.saveAndFlush(user);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        CustomUserDetails customUserDetails = ((CustomUserDetails) authentication.getPrincipal());
        
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(customUserDetails.getUserId());
        
        return new ResponseEntity<>(new AuthResponseDto(token, refreshToken.getToken(),
        customUserDetails.getUsername(), customUserDetails.getUserId()), HttpStatus.OK);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findByToken(requestRefreshToken);
            
            if (!refreshTokenOptional.isPresent()) {
                return new ResponseEntity<>("Invalid refresh token", HttpStatus.BAD_REQUEST);
            }
            
            RefreshToken refreshToken = refreshTokenOptional.get();
            
            // Verify token expiration
            refreshTokenService.verifyExpiration(refreshToken);
            
            // Get user and generate new token
            UserEntity user = refreshToken.getUser();
            String token = jwtGenerator.generateTokenFromUsername(user.getUsername());
            
            return new ResponseEntity<>(new AccessTokenResponse(token), HttpStatus.OK);
            
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
        }
    }
}
