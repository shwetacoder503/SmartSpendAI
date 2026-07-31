package com.example.smartspendai.data.model;

public class TokenResponse {
    private String access_token;
    private String token_type;
    private UserResponse user;

    public String getAccessToken() { return access_token; }
    public String getTokenType() { return token_type; }
    public UserResponse getUser() { return user; }
}
