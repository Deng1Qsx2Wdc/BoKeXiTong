package com.example.demo.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
//@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String Type = "Bearer ";
    private long expiresIn; // access token过期时间（秒）

    public AuthResponse(){

    }

    public AuthResponse(String accessToken, String refreshToken, String Type, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.Type = Type;
        this.expiresIn = expiresIn;
    }
}
