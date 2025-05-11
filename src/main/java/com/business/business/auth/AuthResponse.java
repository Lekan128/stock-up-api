package com.business.business.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AuthResponse {
    public String accessToken;
    public String refreshToken;

    public static AuthResponse getInstance(String accessToken, String refreshToken){
        return new AuthResponse(accessToken, refreshToken);
    }
}
