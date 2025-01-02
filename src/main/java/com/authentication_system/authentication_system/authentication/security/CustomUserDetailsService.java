package com.authentication_system.authentication_system.authentication.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.authentication_system.authentication_system.authentication.model.AuthProvider;
import com.authentication_system.authentication_system.authentication.model.UserEntity;
import com.authentication_system.authentication_system.authentication.repository.UserRepository;



@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        if (!user.isEmailVerified() && user.getProvider() == AuthProvider.LOCAL) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        return new CustomUserDetails(user);
    }

}
