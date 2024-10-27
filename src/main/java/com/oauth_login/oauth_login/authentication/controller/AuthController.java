package com.oauth_login.oauth_login.authentication.controller;

import com.oauth_login.oauth_login.authentication.dto.AuthResponseDto;
import com.oauth_login.oauth_login.authentication.dto.LoginDto;
import com.oauth_login.oauth_login.authentication.dto.RegisterDto;
import com.oauth_login.oauth_login.authentication.model.Role;
import com.oauth_login.oauth_login.authentication.model.UserEntity;
import com.oauth_login.oauth_login.authentication.repository.RoleRepository;
import com.oauth_login.oauth_login.authentication.repository.UserRepository;
import com.oauth_login.oauth_login.authentication.security.CustomUserDetails;
import com.oauth_login.oauth_login.authentication.security.JWTGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTGenerator jwtGenerator;


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

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            return new ResponseEntity<>("Email is taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Optional<Role> roles = roleRepository.findByName("USER");
        roles.ifPresent(role -> user.setRoles(Collections.singletonList(role)));

        userRepository.saveAndFlush(user);
        return new ResponseEntity<>("User Registered with success!", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtGenerator.generateToken(authentication);
        CustomUserDetails customUserDetails = ((CustomUserDetails) authentication.getPrincipal());
        return new ResponseEntity<>(new AuthResponseDto(token, loginDto.getUsername(), customUserDetails.getUserId()), HttpStatus.OK);
    }
}
