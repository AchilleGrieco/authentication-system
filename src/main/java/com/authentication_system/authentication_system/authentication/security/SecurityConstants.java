package com.authentication_system.authentication_system.authentication.security;

public class SecurityConstants {

    public static final long JWT_EXPIRATION = 900000; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    public static final String JWT_SECRET = "secret";
}
