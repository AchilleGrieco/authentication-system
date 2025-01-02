package com.authentication_system.authentication_system.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.authentication_system.authentication_system.authentication.model.AuthProvider;
import com.authentication_system.authentication_system.authentication.model.UserEntity;
import com.authentication_system.authentication_system.authentication.repository.UserRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (OAuth2AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error(ex.getMessage() != null ? ex.getMessage() : "Authentication failed"));
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        
        if(email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("Email not found from OAuth2 provider"));
        }

        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        UserEntity user;
        
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(user.getProvider() == AuthProvider.LOCAL) {
                String errorMessage = "This email is already registered locally. Please use your email and password to login.";
                throw new OAuth2AuthenticationException(new OAuth2Error(errorMessage));
            } else if(!user.getProvider().equals(AuthProvider.GOOGLE)) {
                String errorMessage = "You are signed up with " + user.getProvider() + ". Please use your " + user.getProvider() + " account to login.";
                throw new OAuth2AuthenticationException(new OAuth2Error(errorMessage));
            }
            user = updateExistingUser(user, attributes);
        } else {
            user = registerNewUser(oAuth2UserRequest, attributes);
        }
        
        user.setAttributes(attributes);
        return user;
    }

    private UserEntity registerNewUser(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) {
        UserEntity user = new UserEntity();

        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId((String) attributes.get("sub"));
        user.setName((String) attributes.get("name"));
        user.setEmail((String) attributes.get("email"));
        user.setImageUrl((String) attributes.get("picture"));
        user.setUsername((String) attributes.get("email")); // Using email as username
        return userRepository.save(user);
    }

    private UserEntity updateExistingUser(UserEntity existingUser, Map<String, Object> attributes) {
        existingUser.setName((String) attributes.get("name"));
        existingUser.setImageUrl((String) attributes.get("picture"));
        return userRepository.save(existingUser);
    }
}
