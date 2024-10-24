package com.oauth_login.oauth_login.authentication.security;

import com.oauth_login.oauth_login.authentication.model.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CustomUserDetails implements UserDetails {

    private UserEntity userEntity;

    //private Collection<GrantedAuthority> grantedAuthorities;

    public CustomUserDetails(UserEntity userEntity/*, Collection<GrantedAuthority> grantedAuthorities*/) {
        this.userEntity = userEntity;
        // this.grantedAuthorities = grantedAuthorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
        // return this.grantedAuthorities;
    }

    public Long getUserId() {
        return userEntity.getUserId();
    }

    @Override
    public String getPassword() {
        return userEntity.getPassword();
    }

    @Override
    public String getUsername() {
        return userEntity.getUsername();
    }

    public String getEmail() {
        return userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
