package com.oauth_login.oauth_login.authentication.controller;

import com.oauth_login.oauth_login.authentication.dto.AuthResponseDto;
import com.oauth_login.oauth_login.authentication.dto.LoginDto;
import com.oauth_login.oauth_login.authentication.dto.RegisterDto;
import com.oauth_login.oauth_login.authentication.dto.TokenRefreshRequest;
import com.oauth_login.oauth_login.authentication.model.AuthProvider;
import com.oauth_login.oauth_login.authentication.model.RefreshToken;
import com.oauth_login.oauth_login.authentication.model.UserEntity;
import com.oauth_login.oauth_login.authentication.model.VerificationToken;
import com.oauth_login.oauth_login.authentication.repository.UserRepository;
import com.oauth_login.oauth_login.authentication.repository.VerificationTokenRepository;
import com.oauth_login.oauth_login.authentication.security.CustomUserDetails;
import com.oauth_login.oauth_login.authentication.security.JWTGenerator;
import com.oauth_login.oauth_login.authentication.service.EmailService;
import com.oauth_login.oauth_login.authentication.service.RefreshTokenService;
import com.oauth_login.oauth_login.authentication.service.VerificationTokenCleanupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenCleanupService verificationTokenCleanupService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         JWTGenerator jwtGenerator,
                         RefreshTokenService refreshTokenService,
                         EmailService emailService,
                         VerificationTokenRepository verificationTokenRepository,
                         VerificationTokenCleanupService verificationTokenCleanupService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
        this.verificationTokenRepository = verificationTokenRepository;
        this.verificationTokenCleanupService = verificationTokenCleanupService;
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

        // Check if we can generate a new token for this email
        if (!verificationTokenCleanupService.canGenerateTokenForEmail(registerDto.getEmail())) {
            return new ResponseEntity<>("Too many verification attempts for this email. Please wait for previous tokens to expire.", 
                HttpStatus.TOO_MANY_REQUESTS);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setEmailVerified(false);
        
        user = userRepository.saveAndFlush(user);

        // Create verification token with configured expiration
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(
            token, 
            user, 
            verificationTokenCleanupService.calculateExpirationDate()
        );
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), token);

        return new ResponseEntity<>("User registered successfully. Please check your email for verification.", HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        
        if (verificationToken.isEmpty()) {
            return new ResponseEntity<>("Invalid verification token", HttpStatus.BAD_REQUEST);
        }

        VerificationToken tokenEntity = verificationToken.get();
        if (tokenEntity.isExpired()) {
            return new ResponseEntity<>("Verification token has expired", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.saveAndFlush(user);
        verificationTokenRepository.delete(tokenEntity);

        return new ResponseEntity<>("Email verified successfully", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            CustomUserDetails customUserDetails = ((CustomUserDetails) authentication.getPrincipal());
            
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(customUserDetails.getUserId());
            
            return new ResponseEntity<>(new AuthResponseDto(token, refreshToken.getToken(), "Bearer",
                customUserDetails.getUsername(), customUserDetails.getUserId(), null), HttpStatus.OK);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Please verify your email")) {
                return new ResponseEntity<>(new AuthResponseDto("Please verify your email before logging in"), HttpStatus.FORBIDDEN);
            }
            return new ResponseEntity<>(new AuthResponseDto("Invalid username or password"), HttpStatus.BAD_REQUEST);
        }
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
            
            return new ResponseEntity<>(new AuthResponseDto(token, refreshToken.getToken(), "Bearer", user.getUsername(), user.getUserId()), HttpStatus.OK);
            
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Refresh token was expired. Please make a new signin request", HttpStatus.UNAUTHORIZED);
        }
    }
}
